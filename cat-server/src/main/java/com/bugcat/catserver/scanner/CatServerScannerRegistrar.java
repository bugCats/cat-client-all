package com.bugcat.catserver.scanner;

import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.catserver.annotation.CatServer;
import com.bugcat.catserver.annotation.EnableCatServer;
import com.bugcat.catserver.utils.CatServerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 扫描自定义注解
 *
 * 注意，在装载此类时，其生命周期早于Spring容器，任何自动注入注解都是无效的！包括@Autowired @Resource @Value
 *
 * @author: bugcat
 * */
public class CatServerScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    
    private static Logger log = LoggerFactory.getLogger(CatServerScannerRegistrar.class);

    //资源加载器
    private ResourceLoader resourceLoader;
    
    //所有被@CatServer标记的类
    private List<Object> catServerList = new ArrayList<>();
    
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

        Class<?>[] classes = annoAttrs.getClassArray("classes");
        if( classes.length > 0 ){
            
            log.info("catServer 服务端数量：" + classes.length );
            registerCatServer(classes, registry);
            
        } else {

            String[] pkgs = annoAttrs.getStringArray("value");
            if( pkgs.length == 1 && CatToosUtil.isBlank(pkgs[0]) ){//如果没有设置扫描包路径，取启动类路径
                StandardAnnotationMetadata annotationMetadata = (StandardAnnotationMetadata) metadata;
                Class<?> stratClass = annotationMetadata.getIntrospectedClass();    //启动类class
                String basePackage = stratClass.getPackage().getName();
                pkgs = new String[] {basePackage};  //获取启动类所在包路径
            }

            // 定义扫描对象
            CatServerScanner scanner = new CatServerScanner(registry);
            scanner.setResourceLoader(resourceLoader);
            scanner.addIncludeFilter(new AnnotationTypeFilter(CatServer.class));   //筛选带有@CatServer注解的类
            scanner.scan(pkgs);
            if( scanner.definitions == null ){
                scanner.definitions = new HashSet<>();
            }
            log.info("catServer 服务端数量：" + scanner.definitions.size() );
            for ( AbstractBeanDefinition definition : scanner.definitions ) {
                beanDefinition(definition);
            }
        }

        
        BeanDefinitionBuilder catServerUtil = BeanDefinitionBuilder.genericBeanDefinition(CatServerUtil.class);
        registry.registerBeanDefinition(CatServerUtil.class.getSimpleName(), catServerUtil.getBeanDefinition());
        
        
        BeanDefinitionBuilder catServerInitBean = BeanDefinitionBuilder.genericBeanDefinition(CatServerInitBean.class);
        catServerInitBean.addPropertyValue("catServerList", catServerList);
        catServerInitBean.addPropertyValue("registry", registry);
        registry.registerBeanDefinition(CatServerInitBean.class.getSimpleName(), catServerInitBean.getBeanDefinition());

        
        try {
            // swagger扫描
            Class swagger = Class.forName("com.bugcat.catserver.utils.CatSwaggerScanner");
            BeanDefinitionBuilder catProvider = BeanDefinitionBuilder.genericBeanDefinition(swagger);
            catProvider.getBeanDefinition().setPrimary(true);
            registry.registerBeanDefinition(swagger.getSimpleName(), catProvider.getBeanDefinition());
        } catch ( Exception e ) {
            
        }
    }
    
    /**
     * 注册工厂
     * */
    private void registerCatServer(Class<?>[] classes, BeanDefinitionRegistry registry) {
        for ( Class<?> clazz : classes ){
            CatServer server = clazz.getAnnotation(CatServer.class);
            String beanName = CatToosUtil.defaultIfBlank(server.value(), CatToosUtil.uncapitalize(clazz.getSimpleName()));
            AbstractBeanDefinition definition = (AbstractBeanDefinition) registry.getBeanDefinition(beanName);
            if( definition == null ){
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
                definition = builder.getRawBeanDefinition();
                registry.registerBeanDefinition(beanName, definition);
            }
            beanDefinition(definition);
        }
    }

    private String beanDefinition(AbstractBeanDefinition definition){

        String className = definition.getBeanClassName();   //扫描到的interface类名
        
        /**
         * 此处有个很迷惑的操作
         * definition.getBeanClass() 看似返回类的class，但是由于此时类未加载，实际上class不存在
         * 执行这个方法时，会报错[has not been resolved into an actual Class]
         * 但是如果在其他类又必须需要class
         * 可以通过 definition.getPropertyValues().addPropertyValue("clazz", className) 形式负责
         * 但是此时 className 为String字符串，在其他类中却可以用Class属性接收！
         * 只能说[org.springframework.beans.PropertyValue]很强大吧
         * */
        catServerList.add(className);

        definition.setBeanClass(CatServerFactoryBean.class);    //FactoryBean类型
        definition.getPropertyValues().addPropertyValue("clazz", className);    //FactoryBean属性
        definition.setPrimary(true);
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);    //生成的对象，支持@Autowire自动注入
        
        return className;
    }
    
    

    private static class CatServerScanner extends ClassPathBeanDefinitionScanner {

        private Set<AbstractBeanDefinition> definitions = new HashSet<>();
        
        
        public CatServerScanner(BeanDefinitionRegistry registry) {
            super(registry);
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

            /**
             * @CatServer 注解包含了 元注解@Component
             * 在执行自定义扫描时，CatServer类已经被Spring扫描了，使用了默认FactoryBean！
             * 此时需要将之前扫描的BeanDefinition获取到，重新设置FactoryBean
             * */
            BeanDefinitionRegistry registry = super.getRegistry();
            BeanDefinition definition = registry.getBeanDefinition(beanName);
            if( definition != null ){
                definitions.add((AbstractBeanDefinition) definition);
            } else {
                definitions.add(beanDefinition);
            }
            
        }
    }


}
