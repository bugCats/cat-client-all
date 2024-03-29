package cc.bugcat.catclient.scanner;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.annotation.EnableCatClient;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatClientProvider;
import cc.bugcat.catclient.spi.CatSendInterceptor;
import cc.bugcat.catclient.utils.CatClientUtil;
import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catface.utils.CatToosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * 扫描自定义注解
 *
 * 注意，在装载此类时，其生命周期早于Spring容器，任何自动注入注解都是无效的！包括@Autowired @Resource @Value
 *
 * @author: bugcat
 * */
public class CatClientScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private final Logger logger = LoggerFactory.getLogger(CatClientScannerRegistrar.class);


    //资源加载器
    private ResourceLoader resourceLoader;



    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }



    /**
     * 注册扫描事件
     * */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        logger.info("catclient 客户端启用...");

        /**
         * 这个类AnnotationScannerRegistrar，通过{@link EnableCatClient}注解上使用@Import加载
         * metadata就是被@EnableCatClient注解的对象，即：启动类
         * */
        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(EnableCatClient.class.getName()));

        // 扫描包路径
        String[] scanPackages = CatToosUtil.scanPackages(metadata, annoAttrs);

        // 客户端数量
        int count = 0;

        // 通过class直接注册
        Class[] classes = annoAttrs.getClassArray("classes");
        count = count + registerCatClient(classes, registry);

        // 定义扫描对象
        CatClientScanner scanner = new CatClientScanner(registry);
        scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(CatClient.class));   //筛选带有@CatClient注解的类
        scanner.scan(scanPackages);
        scanner.holders.forEach(holder -> {
            GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
            beanDefinition(null, definition);
        });

        count = count + scanner.holders.size();
        logger.info("catclient 客户端数量：" + count );

        BeanRegistry beanRegistry = new BeanRegistry(resourceLoader, registry, scanPackages);
        
        // 扫描所有 CatClientFactory 子类
        beanRegistry.scannerByClass(CatClientFactory.class);

        // 扫描所有 CatMethodInterceptor 子类
        beanRegistry.scannerByClass(CatSendInterceptor.class);

        // 扫描所有 AbstractResponesWrapper 子类
        beanRegistry.scannerByClass(AbstractResponesWrapper.class);
        
        // CatClientConfiguration全局默认配置
        beanRegistry.registerBean(annoAttrs.getClass("configuration"));

        // spring 容器
        beanRegistry.registerBean(CatClientUtil.class);
        
        // 客户端前置依赖项
        beanRegistry.registerBean(CatClientDependFactoryBean.BEAN_NAME, CatClientDependFactoryBean.class);

    }




    /**
     * 通过class直接注册
     * */
    private int registerCatClient(Class[] inters, BeanDefinitionRegistry registry) {
        int count = 0;
        for ( Class<?> inter : inters ){
            if( CatClientProvider.class.isAssignableFrom(inter) ){ // interface是CatClients子类，表示使用method批量注册
                Method[] methods = inter.getMethods();
                for ( Method method : methods ) {
                    CatClient catClient = method.getAnnotation(CatClient.class);
                    if( catClient == null ){
                        continue;
                    }
                    count ++ ;
                    Class clazz = method.getReturnType();
                    registerClass(catClient, clazz, registry);
                }
            } else {    //单个指定interface注册
                CatClient client = inter.getAnnotation(CatClient.class);
                if( client == null ){
                    logger.warn(inter.getName() + "上没有找到@CatClient注解");
                    continue;
                }
                count ++ ;
                registerClass(client, inter, registry);
            }
        }
        return count;
    }

    /**
     * 注册interface
     * */
    private void registerClass(CatClient catClient, Class interfaceClass, BeanDefinitionRegistry registry){
        String beanName = CatToosUtil.defaultIfBlank(catClient.value(), CatToosUtil.uncapitalize(interfaceClass.getSimpleName()));
        AbstractBeanDefinition definition = null;
        try { definition = (AbstractBeanDefinition) registry.getBeanDefinition(beanName); } catch ( Exception e ) { }
        if( definition == null ){
            definition = new GenericBeanDefinition();
            definition.setBeanClass(interfaceClass);
            registry.registerBeanDefinition(beanName, definition);
        }
        beanDefinition(catClient, definition);
    }



    /**
     * 修改CatClientInfoFactoryBean相关依赖配置
     * */
    private String beanDefinition(CatClient catClient, AbstractBeanDefinition definition){

        String interfaceName = definition.getBeanClassName();   //扫描到的interface类名

        /**
         * 注册FactoryBean工厂，
         * 自动注入，会根据interface，从Spring容器中获取到对应的组件，这需要FactoryBean支持
         * interface -> interface工厂 -> interface实现类 -> 自动注入
         * FactoryBean 的生命周期，早于Spring容器，无法使用自动注入，因此需要使用ioc反射，将对象赋值给FactoryBean
         * */
        definition.setBeanClass(CatClientInfoFactoryBean.class);
        definition.getPropertyValues().addPropertyValue("interfaceClass", interfaceName);
        definition.getPropertyValues().addPropertyValue("catClient", catClient);
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);    //生成的对象，支持@Autowire自动注入
        definition.setDependsOn(CatClientDependFactoryBean.BEAN_NAME);
        definition.setPrimary(true);
        definition.setLazyInit(true);
        return interfaceName;
    }


    private static class BeanRegistry {

        private final ResourceLoader resourceLoader;
        private final BeanDefinitionRegistry registry;
        private final String[] packages;

        private BeanRegistry(ResourceLoader resourceLoader, BeanDefinitionRegistry registry, String[] packages) {
            this.resourceLoader = resourceLoader;
            this.registry = registry;
            this.packages = packages;
        }

        /**
         * 注册指定class
         * */
        private void registerBean(Class beanClass){
            registerBean(CatToosUtil.uncapitalize(beanClass.getSimpleName()), beanClass);
        }
        private void registerBean(String beanName, Class beanClass){
            AbstractBeanDefinition definition = new GenericBeanDefinition();
            definition.setBeanClass(beanClass);
            definition.setPrimary(true);
            registry.registerBeanDefinition(beanName, definition);
        }


        /**
         * 扫描指定class的子类
         * */
        private void scannerByClass(Class clazz){
            ClassPathBeanDefinitionScanner interceptorScanner = new ClassPathBeanDefinitionScanner(registry);
            interceptorScanner.setResourceLoader(resourceLoader);
            interceptorScanner.addIncludeFilter(new AssignableTypeFilter(clazz));
            interceptorScanner.scan(packages);
        }
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
