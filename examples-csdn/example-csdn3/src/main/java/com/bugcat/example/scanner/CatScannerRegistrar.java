package com.bugcat.example.scanner;

import com.bugcat.example.annotation.ApiCtrl;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author: bugcat
 * */
public class CatScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    
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
        
        String pkgs = "com.bugcat.example";

        //所有被@ApiCtrl标记的类
        Set<Object> servers = new HashSet<>();
        
        // 定义扫描对象
        CatScanner scanner = new CatScanner(servers, registry);
        scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(ApiCtrl.class));   //筛选带有@ApiCtrl注解的类

        //执行扫描
        scanner.scan(pkgs);

        BeanDefinitionBuilder ctrlInit = BeanDefinitionBuilder.genericBeanDefinition(CatCtrlInitializingBean.class);
        ctrlInit.addConstructorArgValue(servers);
        registry.registerBeanDefinition("catCtrlInitializingBean", ctrlInit.getBeanDefinition());

    }
    

    
    /**
     * 自定义扫描
     * */
    private static class CatScanner extends ClassPathBeanDefinitionScanner {

        private Set<Object> servers;
        
        public CatScanner(Set<Object> servers, BeanDefinitionRegistry registry) {
            super(registry);
            this.servers = servers;
        }
        
        /**
         * ApiCtrl标记在interface上，spring扫描时，默认会排除interface，因此需要重写此方法
         * */
        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            AnnotationMetadata metadata = beanDefinition.getMetadata();
            if (metadata.isIndependent()) {
                if ( !metadata.isAnnotation() && metadata.hasAnnotation(ApiCtrl.class.getName())) {
                    servers.add(beanDefinition.getBeanClassName());
                }
            }
            return super.isCandidateComponent(beanDefinition);
        }
    }

    
}
