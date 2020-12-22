package com.bugcat.catclient.scanner;

import com.bugcat.catclient.annotation.CatClient;
import com.bugcat.catclient.annotation.EnableCatClient;
import com.bugcat.catclient.beanInfos.CatClientInfo;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.catclient.spi.CatDefaultConfiguration;
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
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.HashSet;
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
    private EnvironmentProperty prop;

    private CatDefaultConfiguration config;
    
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.prop = new EnvironmentProperty(environment);
    }


    /**
     * 注册扫描事件
     * */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        
        // 这个类AnnotationScannerRegistrar，通过@EnableCatClient注解上使用@Import加载
        // metadata就是被@EnableCatClient注解的对象，即：启动类
        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(EnableCatClient.class.getName()));

        try {
            Class<? extends CatDefaultConfiguration> configClass = annoAttrs.getClass("defaults");
            config = configClass.newInstance();
            CatClientUtil.registerBean(CatDefaultConfiguration.class, config);
        } catch ( Exception e ) {
            log.error("初始化CatDefaultConfiguration异常", e);
            return;
        }
        log.info("catclient 客户端启用...");
        
        
        Class<?>[] classes = annoAttrs.getClassArray("classes");
        if( classes.length > 0 ){
            
            log.info("catclient 客户端数量：" + classes.length );
            registerCatClient(classes, registry);
            
        } else {
            
            String[] pkgs = annoAttrs.getStringArray("value");
            if( pkgs.length == 1 && CatToosUtil.isBlank(pkgs[0]) ){//如果没有设置扫描包路径，取启动类路径
                StandardAnnotationMetadata annotationMetadata = (StandardAnnotationMetadata) metadata;
                Class<?> stratClass = annotationMetadata.getIntrospectedClass();    //启动类class
                String basePackage = stratClass.getPackage().getName();
                pkgs = new String[] {basePackage};  //获取启动类所在包路径
            }

            // 定义扫描对象
            CatClientScanner scanner = new CatClientScanner(registry);
            scanner.setResourceLoader(resourceLoader);
            scanner.addIncludeFilter(new AnnotationTypeFilter(CatClient.class));   //筛选带有@CatClient注解的类

            //执行扫描
            scanner.scan(pkgs);
            
            if( scanner.holders == null ){
                scanner.holders = new HashSet<>();
            }
            log.info("catclient 客户端数量：" + scanner.holders.size() );
            for ( BeanDefinitionHolder holder : scanner.holders ){
                registerCatClient(holder, registry);
            }
        }
        
        
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(CatClientUtil.class);
        registry.registerBeanDefinition(CatClientUtil.class.getSimpleName(), builder.getBeanDefinition());
        
    }
    
    
    /**
     * 扫描注解注册
     * */
    private void registerCatClient(BeanDefinitionHolder holder, BeanDefinitionRegistry registry) {
        
        GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();

        AnnotationMetadata beanMetadata = ((AnnotatedBeanDefinition) definition).getMetadata();
        AnnotationAttributes attr = AnnotationAttributes.fromMap(beanMetadata.getAnnotationAttributes(CatClient.class.getName()));
        beanDefinition(attr, definition);
    }

    
    /**
     * 通过class直接注册
     * */
    private void registerCatClient(Class<?>[] classes, BeanDefinitionRegistry registry) {
        for ( Class<?> clazz : classes ){
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
            AbstractBeanDefinition definition = builder.getRawBeanDefinition();
            StandardAnnotationMetadata metadata = new StandardAnnotationMetadata(clazz);
            AnnotationAttributes attr = new AnnotationAttributes(metadata.getAnnotationAttributes(CatClient.class.getName()));
            CatClientInfo info = beanDefinition(attr, definition);
            registry.registerBeanDefinition(info.getBeanName(), definition);
        }
    }
    
    
    private CatClientInfo beanDefinition(AnnotationAttributes attr, AbstractBeanDefinition definition){

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
        attr.put("beanName", className.substring(className.lastIndexOf(".") + 1));
        attr.put("config", config); //全局的默认配置
        
        CatClientInfo catClientInfo = new CatClientInfo(attr, prop);
        
        // 注册FactoryBean工厂，
        // 自动注入，会根据interface，从Spring容器中获取到对应的组件，这需要FactoryBean支持
        // interface -> interface工厂 -> interface实现类 -> 自动注入
        // FactoryBean 的生命周期，早于Spring容器，无法使用自动注入，因此需要使用ioc反射，将对象赋值给FactoryBean
        definition.setBeanClass(CatClientInfoFactoryBean.class);    //FactoryBean类型
        definition.getPropertyValues().addPropertyValue("catClientInfo", catClientInfo);      //FactoryBean属性
        definition.getPropertyValues().addPropertyValue("prop", prop);    //FactoryBean属性
        definition.getPropertyValues().addPropertyValue("clazz", className);    //FactoryBean属性
        definition.setPrimary(true);
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);    //生成的对象，支持@Autowire自动注入
        
        return catClientInfo;
    }
    

    private static class EnvironmentProperty extends Properties {
        
        private Environment environment;

        public EnvironmentProperty(Environment environment) {
            super();
            this.environment = environment;
        }
        
        /**
         * key 类似于 ${demo.remoteApi}
         * */
        @Override
        public String getProperty(String key) {
            return environment.resolvePlaceholders(key);
        }
        @Override
        public String getProperty(String key, String defaultValue) {
            String value = environment.resolvePlaceholders(key);
            return defaultValue != null && key.equals(value) ? defaultValue : value; 
        }
    }

    
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
         * @CatClient 标记在interface上，spring扫描时，默认会排除interface，因此需要重写此方法
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
