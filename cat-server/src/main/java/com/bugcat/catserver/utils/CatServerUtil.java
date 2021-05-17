package com.bugcat.catserver.utils;

import com.bugcat.catserver.handler.CatfaceHandlerMethodArgumentResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;
import java.util.Map;


/**
 *
 * @author bugcat
 * */
@ComponentScan("com.bugcat.catserver")
public class CatServerUtil implements ApplicationContextAware{


    private static ApplicationContext context;

    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
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



    @Configuration
    public static class CatWebConfig extends WebMvcConfigurerAdapter{
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
            argumentResolvers.add(new CatfaceHandlerMethodArgumentResolver());
            super.addArgumentResolvers(argumentResolvers);
        }
        
    }
}
