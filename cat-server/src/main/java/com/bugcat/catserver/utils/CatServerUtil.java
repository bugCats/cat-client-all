package com.bugcat.catserver.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 *
 * @author bugcat
 * */
@ComponentScan("com.bugcat.catserver")
public class CatServerUtil implements ApplicationContextAware{

    public static final String bridgeName = "$bugcat$";
    public static final String annName = RequestMapping.class.getName();
    
    
    private static ApplicationContext context;



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static AutowireCapableBeanFactory getBeanFactory(){
        return context.getAutowireCapableBeanFactory();
    }
    
    
    public static <T> T getBean (Class<T> clazz){
        try {
            return context.getBean(clazz);
        } catch ( Exception e ) {
            Map<String, T> beans = context.getBeansOfType(clazz);
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
    }
    
    
    public static ClassLoader getClassLoader(){
        return context.getClassLoader();
    }
    
    
    private static ConcurrentMap<Class, Object> ctrlMap = new ConcurrentHashMap<>();
    private static Map<Class, Class> serverMap = new HashMap<>();
    
    public static void setCtrlClass(Class server, Object ctrl){
        ctrlMap.put(server, ctrl);
        serverMap.put(ctrl.getClass(), server);
    }
    public static Object getCtrlClass(Class server){
        Object ctrl = ctrlMap.remove(server);
        return ctrl;
    }
    public static Class getServerClass(Class ctrl){
        return serverMap.get(ctrl);
    }
    
}
