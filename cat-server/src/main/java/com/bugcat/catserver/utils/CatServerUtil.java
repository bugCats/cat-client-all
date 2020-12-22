package com.bugcat.catserver.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author bugcat
 * */
public class CatServerUtil implements ApplicationContextAware, InitializingBean{


    private static Queue<InitializingBean> initializingBeans = new ConcurrentLinkedQueue<>();
    
    private static ApplicationContext context;

    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        doInitBean();
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
    
    
    
    public static Object getBean (String name){
        try {
            return context.getBean(name);
        } catch ( Exception e ) {
            return null;
        }
    }

    
    public static void addInitBean(InitializingBean bean){
        initializingBeans.add(bean);
    }
    
    public static void doInitBean() throws Exception {
        while ( true ) {
            InitializingBean bean = CatServerUtil.initializingBeans.poll();
            if( bean == null ){
                break;
            }
            bean.afterPropertiesSet();
        }
    }
    
}
