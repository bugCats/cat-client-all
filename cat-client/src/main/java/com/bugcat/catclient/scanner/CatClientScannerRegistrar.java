package com.bugcat.catclient.scanner;

import com.bugcat.catclient.annotation.CatClient;
import com.bugcat.catclient.annotation.EnableCatClient;
import com.bugcat.catclient.handler.CatClients;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.catclient.spi.CatClientFactory;
import com.bugcat.catclient.spi.CatHttp;
import com.bugcat.catclient.spi.DefaultConfiguration;
import com.bugcat.catclient.utils.CatClientUtil;
import com.bugcat.catface.utils.CatToosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * 扫描自定义注解
 * 
 * 注意，在装载此类时，其生命周期早于Spring容器，任何自动注入注解都是无效的！包括@Autowired @Resource @Value
 * 
 * @author: bugcat
 * */
public class CatClientScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

    private static Logger log = LoggerFactory.getLogger(SendProcessor.class);
    
    
    //资源加载器
    private ResourceLoader resourceLoader;
    
    
    //获取properties文件中的参数
    private Properties prop;
    
    
    //CatClientInfoFactoryBean的依赖项
    private String[] dependsOn;
    
    
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.prop = CatClientUtil.envProperty(environment);
    }


    /**
     * 注册扫描事件
     * */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        log.info("catclient 客户端启用...");
        
        /**
         * 这个类AnnotationScannerRegistrar，通过{@link EnableCatClient}注解上使用@Import加载
         * metadata就是被@EnableCatClient注解的对象，即：启动类
         * */
        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(EnableCatClient.class.getName()));

        
        List<String> dependsOn = new ArrayList<>();
        
        /**
         * 全局默认配置
         * */
        Class<? extends DefaultConfiguration> configClass = annoAttrs.getClass("defaults");
        BeanDefinitionBuilder config = BeanDefinitionBuilder.genericBeanDefinition(configClass);
        String configBeanName = CatToosUtil.uncapitalize(configClass.getSimpleName());
        registry.registerBeanDefinition(configBeanName, config.getBeanDefinition());
        dependsOn.add(configBeanName);
        
        /**
         * 工具类
         * */
        BeanDefinitionBuilder catClientUtil = BeanDefinitionBuilder.genericBeanDefinition(CatClientUtil.class);
        String catClientUtilBeanName = CatToosUtil.uncapitalize(CatClientUtil.class.getSimpleName());
        registry.registerBeanDefinition(catClientUtilBeanName, catClientUtil.getBeanDefinition());
        dependsOn.add(catClientUtilBeanName);
        
        this.dependsOn = dependsOn.toArray(new String[dependsOn.size()]);
        
        int count = 0;
        
        // 通过class直接注册
        Class<?>[] classes = annoAttrs.getClassArray("classes");
        if( classes.length > 0 ){
            count = count + registerCatClient(classes, registry);
        }

        String[] pkgs = CatToosUtil.scanPackages(metadata, annoAttrs, "value");

        // 定义扫描对象
        CatClientScanner scanner = new CatClientScanner(registry);
        scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(CatClient.class));   //筛选带有@CatClient注解的类

        //执行扫描
        scanner.scan(pkgs);
        if( scanner.holders == null ){
            scanner.holders = new HashSet<>();
        }
        
        count = count + scanner.holders.size();
        for ( BeanDefinitionHolder holder : scanner.holders ){
            GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
            beanDefinition(null, definition);
        }

        log.info("catclient 客户端数量：" + count );
        
        
        //扫描所有 CatClientFactory 子类
        ClassPathBeanDefinitionScanner factoryScanner = new ClassPathBeanDefinitionScanner(registry);
        factoryScanner.setResourceLoader(resourceLoader);
        factoryScanner.addIncludeFilter(CatToosUtil.typeChildrenFilter(CatClientFactory.class));
        factoryScanner.scan(pkgs);
        
        //扫描所有的 CatHttp 子类
        ClassPathBeanDefinitionScanner catHttpScanner = new ClassPathBeanDefinitionScanner(registry);
        catHttpScanner.setResourceLoader(resourceLoader);
        catHttpScanner.addIncludeFilter(CatToosUtil.typeChildrenFilter(CatHttp.class));
        catHttpScanner.scan(pkgs);
    }
    
    

    
    /**
     * 通过class直接注册
     * */
    private int registerCatClient(Class<?>[] inters, BeanDefinitionRegistry registry) {
        int count = 0;
        for ( Class<?> inter : inters ){
            if( CatClients.class.isAssignableFrom(inter) ){ // interface是CatClients子类，表示使用method批量注册
                Method[] methods = inter.getMethods();
                for ( Method method : methods ) {
                    CatClient client = method.getAnnotation(CatClient.class);
                    if( client == null ){
                        continue;
                    }
                    count ++ ;
                    Class clazz = method.getReturnType();
                    registerClass(client, clazz, registry);
                }
            } else {    //单个指定interface注册
                CatClient client = inter.getAnnotation(CatClient.class);
                if( client == null ){
                    log.warn(inter.getName() + "上没有找到@CatClient注解");
                    continue;
                }
                count ++ ;
                registerClass(client, inter, registry);
            }
        }
        return count;
    }
    
    private void registerClass(CatClient client, Class clazz, BeanDefinitionRegistry registry){
        String beanName = CatToosUtil.defaultIfBlank(client.value(), CatToosUtil.uncapitalize(clazz.getSimpleName()));
        AbstractBeanDefinition definition = null;
        try { definition = (AbstractBeanDefinition) registry.getBeanDefinition(beanName); } catch ( Exception e ) { }
        if( definition == null ){
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
            definition = builder.getRawBeanDefinition();
            registry.registerBeanDefinition(beanName, definition);
        }
        beanDefinition(client, definition);
    }
    
    
    
    
    private String beanDefinition(CatClient client, AbstractBeanDefinition definition){

        String className = definition.getBeanClassName();   //扫描到的interface类名
        
        /**
         * 此处有个很迷惑的操作
         * definition.getBeanClass() 看似返回类的class，但是由于此时类未加载，实际上class不存在
         * 执行这个方法时，会报错[has not been resolved into an actual Class]
         * 但是如果在其他类又必须需要class
         * 可以通过 definition.getPropertyValues().addPropertyValue("clazz", className) 形式赋值
         * 注意，此时className为String字符串，在其他类中却可以用Class属性接收！
         * 只能说[org.springframework.beans.PropertyValue]很强大吧
         * */
        
        
        /**
         * 注册FactoryBean工厂，
         * 自动注入，会根据interface，从Spring容器中获取到对应的组件，这需要FactoryBean支持
         * interface -> interface工厂 -> interface实现类 -> 自动注入
         * FactoryBean 的生命周期，早于Spring容器，无法使用自动注入，因此需要使用ioc反射，将对象赋值给FactoryBean
         * */
        definition.setBeanClass(CatClientInfoFactoryBean.class);
        definition.getPropertyValues().addPropertyValue("prop", prop);
        definition.getPropertyValues().addPropertyValue("clazz", className);
        definition.getPropertyValues().addPropertyValue("client", client);
        definition.setPrimary(true);
        definition.setDependsOn(dependsOn); //设置依赖项
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);    //生成的对象，支持@Autowire自动注入
        
        return className;
    }
    
    
    /**
     * 自定义扫描
     * */
    private static class CatClientScanner extends ClassPathBeanDefinitionScanner {

        private Set<BeanDefinitionHolder> holders;
        
        public CatClientScanner(BeanDefinitionRegistry registry) {
            super(registry);
        }
        
        
        @Override
        protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
            holders = super.doScan(basePackages);   //得到所有标记了@CatClient的interface
            return holders;
        }

        
        /**
         * CatClient标记在interface上，spring扫描时，默认会排除interface，因此需要重写此方法
         * */
        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            boolean isCandidate = false;
            AnnotationMetadata metadata = beanDefinition.getMetadata();
            if (metadata.isIndependent()) {
                if ( !metadata.isAnnotation() && metadata.hasAnnotation(CatClient.class.getName())) {
                    isCandidate = true;
                }
            }
            return isCandidate;
        }
    }

    
}
