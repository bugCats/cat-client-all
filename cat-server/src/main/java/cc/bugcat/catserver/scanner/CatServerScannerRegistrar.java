package cc.bugcat.catserver.scanner;

import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.annotation.CatServer;
import cc.bugcat.catserver.annotation.EnableCatServer;
import cc.bugcat.catserver.spi.CatInterceptor;
import cc.bugcat.catserver.utils.CatServerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
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
        String[] pkgs = CatToosUtil.scanPackages(metadata, annoAttrs, "value");

        BeanDefinitionBuilder catServerUtil = BeanDefinitionBuilder.genericBeanDefinition(CatServerUtil.class);
        String catServerUtilBeanName = CatToosUtil.uncapitalize(CatServerUtil.class.getSimpleName());
        registry.registerBeanDefinition(catServerUtilBeanName, catServerUtil.getBeanDefinition());


        //扫描所有的 CatInterceptor 子类
        ClassPathBeanDefinitionScanner interceptorScanner = new ClassPathBeanDefinitionScanner(registry);
        interceptorScanner.setResourceLoader(resourceLoader);
        interceptorScanner.addIncludeFilter(new AssignableTypeFilter(CatInterceptor.class));
        interceptorScanner.scan(pkgs);


        Class[] classes = annoAttrs.getClassArray("classes");
        if( classes.length > 0 ){
            for ( Class<?> clazz : classes ){
                CatServer server = clazz.getAnnotation(CatServer.class);
                String beanName = CatToosUtil.defaultIfBlank(server.value(), CatToosUtil.uncapitalize(clazz.getSimpleName()));
                servers.add(beanName);
            }
        }

        // 定义扫描对象
        CatServerScanner scanner = new CatServerScanner(servers, registry);
        scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(CatServer.class));   //筛选带有@CatServer注解的类
        scanner.scan(pkgs);

        log.info("catServer 服务端数量：" + servers.size() );
        BeanDefinitionBuilder catServerInitBean = BeanDefinitionBuilder.genericBeanDefinition(CatServerInitBean.class);
        catServerInitBean.addConstructorArgValue(servers);
        registry.registerBeanDefinition(CatToosUtil.uncapitalize(CatServerInitBean.class.getSimpleName()), catServerInitBean.getBeanDefinition());


        try {
            // swagger扫描
            Class swagger = Class.forName("cc.bugcat.catserver.utils.CatSwaggerScanner");
            BeanDefinitionBuilder catProvider = BeanDefinitionBuilder.genericBeanDefinition(swagger);
            catProvider.getBeanDefinition().setPrimary(true);
            registry.registerBeanDefinition(CatToosUtil.uncapitalize(swagger.getSimpleName()), catProvider.getBeanDefinition());
        } catch ( Exception e ) {

        }
    }


    /**
     * 自定义扫描
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
