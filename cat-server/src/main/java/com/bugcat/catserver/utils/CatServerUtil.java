package com.bugcat.catserver.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 *
 * @author bugcat
 * */
@ComponentScan("com.bugcat.catserver")
public class CatServerUtil implements ApplicationContextAware {
    
    private static ApplicationContext context;

    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
    
    
    public static <T> T getBean (Class<T> clazz){
        try {
            return context.getBean(clazz);
        } catch ( Exception e ) {
            return null;
        }
    }
    
    
    public static <T> Map<String, T> getBeansOfType (Class<T> clazz){
        try {
            return context.getBeansOfType(clazz);
        } catch ( Exception e ) {
            return null;
        }
    }
    
    
    public static <T> T getBeanOfType (Class<T> clazz){
        Map<String, T> beans = getBeansOfType(clazz);
        if( beans.size() == 1 ){
            return beans.values().iterator().next();
        } else {
            for(T value : beans.values()){
                if( clazz == value.getClass()){
                    return value;
                }
                String clazzName = value.getClass().getSimpleName();
                int start = clazzName.indexOf("$$");
                if( start > -1 ) {
                    clazzName = clazzName.substring(0, start);
                }
                if( clazz.getSimpleName().equals(clazzName) ){
                    return value;
                }
            }
        }
        throw new NoSuchBeanDefinitionException(clazz);
    }
    

    
    /**
     * 给new出的对象，自动注入属性
     * */
    public static void processInjection(Object bean){
        AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
        bpp.setBeanFactory(context.getAutowireCapableBeanFactory());
        bpp.processInjection(bean);
    }
    
    
    public static ClassLoader getClassLoader(){
        return context.getClassLoader();
    }




    public static final void addInitBean(InitializingBean bean){
        BeanInitHandler.beans.add(bean);
    }


    @Order
    @Component
    public static class BeanInitHandler implements InitializingBean {

        private static BlockingQueue<InitializingBean> beans = new LinkedBlockingQueue<>();

        @Override
        public void afterPropertiesSet() throws Exception {
            doInitBean();
        }

        public static final void doInitBean() {
            while ( true ) {
                InitializingBean bean = beans.poll();
                if( bean == null ){
                    break;
                }
                try {
                    bean.afterPropertiesSet();
                } catch ( Exception ex ) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }


    
    
}
