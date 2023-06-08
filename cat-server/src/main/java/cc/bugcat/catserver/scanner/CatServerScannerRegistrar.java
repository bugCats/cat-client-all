package cc.bugcat.catserver.scanner;

import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.annotation.CatServer;
import cc.bugcat.catserver.annotation.EnableCatServer;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.spi.CatResultHandler;
import cc.bugcat.catserver.spi.CatServerInterceptor;
import cc.bugcat.catserver.utils.CatServerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 扫描自定义注解
 *
 * 注意，在装载此类时，其生命周期早于Spring容器，任何自动注入注解都是无效的！包括@Autowired @Resource @Value
 *
 * @author: bugcat
 * */
public class CatServerScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware{

    private static Logger log = LoggerFactory.getLogger(CatServerScannerRegistrar.class);


    //资源加载器
    private ResourceLoader resourceLoader;

    //所有被@CatServer标记的类
    private Set<Object> servers = new HashSet<>();


    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 注册扫描事件
     * */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        log.info("catServer 服务端启用...");

        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(EnableCatServer.class.getName()));

        Class<? extends CatServerConfiguration> configClass = annoAttrs.getClass("configuration");

        String[] scanPackages = CatToosUtil.scanPackages(metadata, annoAttrs);

        // 定义扫描对象
        CatServerScanner scanner = new CatServerScanner(servers, registry);
        scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(CatServer.class));   //筛选带有@CatServer注解的类
        scanner.scan(scanPackages);

        Class[] classes = annoAttrs.getClassArray("classes");
        for ( Class<?> clazz : classes ){
            CatServer server = clazz.getAnnotation(CatServer.class);
            String beanName = CatToosUtil.defaultIfBlank(server.value(), CatToosUtil.uncapitalize(clazz.getSimpleName()));
            servers.add(beanName);
        }

        BeanRegistry beanRegistry = new BeanRegistry(resourceLoader, registry, scanPackages);

        // spring容器
        beanRegistry.registerBean(CatServerUtil.class);
        
        // 扫描所有的 CatInterceptor 子类
        beanRegistry.scannerByClass(CatServerInterceptor.class, CatServerInterceptor.Off.class);

        // 扫描所有 CatResultHandler 子类
        beanRegistry.scannerByClass(CatResultHandler.class);
        
        // 全局配置对象
        beanRegistry.registerBean(configClass);
        
        // CatServer初始化对象
        beanRegistry.registerBean(CatServerFactoryBean.class, definition -> {
            definition.getPropertyValues().addPropertyValue("serverClassSet", servers);
        });

        log.info("catServer 服务端数量：" + servers.size() );

        
        // swagger组件
        CatServerUtil.existClassAndExecute("cc.bugcat.catserver.utils.CatSwaggerScanner", clazz -> {
            beanRegistry.registerBean(clazz, definition -> {
                definition.setPrimary(true);
                definition.setLazyInit(true);
            });
        });

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
            registerBean(CatToosUtil.uncapitalize(beanClass.getSimpleName()), beanClass, definition -> {});
        }
        private void registerBean(Class beanClass, Consumer<AbstractBeanDefinition> consumer){
            registerBean(CatToosUtil.uncapitalize(beanClass.getSimpleName()), beanClass, consumer);
        }
        private void registerBean(String beanName, Class beanClass, Consumer<AbstractBeanDefinition> consumer){
            AbstractBeanDefinition definition = new GenericBeanDefinition();
            definition.setBeanClass(beanClass);
            consumer.accept(definition);
            registry.registerBeanDefinition(beanName, definition);
        }

        /**
         * 扫描指定class的子类
         * */
        private void scannerByClass(Class clazz, Class... excludes){
            ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
            scanner.setResourceLoader(resourceLoader);
            scanner.addIncludeFilter(new AssignableTypeFilter(clazz));
            for ( Class exclude : excludes ) {
                scanner.addExcludeFilter(new AssignableTypeFilter(exclude));
            }
            scanner.scan(packages);
        }
    }



    /**
     * {@code @CatServer} 自定义扫描
     * */
    private static class CatServerScanner extends ClassPathBeanDefinitionScanner{

        private Set<Object> servers;

        public CatServerScanner(Set<Object> servers, BeanDefinitionRegistry registry) {
            super(registry);
            this.servers = servers;
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            boolean isCandidate = false;
            AnnotationMetadata metadata = beanDefinition.getMetadata();
            if (metadata.isIndependent()) {
                if ( !metadata.isAnnotation() && metadata.hasAnnotation(CatServer.class.getName())) {
                    isCandidate = true;
                }
            }
            return isCandidate;
        }

        @Override
        protected void postProcessBeanDefinition(AbstractBeanDefinition beanDefinition, String beanName) {
            super.postProcessBeanDefinition(beanDefinition, beanName);
            servers.add(beanDefinition.getBeanClassName());
        }
    }


}
