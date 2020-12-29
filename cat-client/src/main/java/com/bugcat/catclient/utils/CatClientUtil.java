package com.bugcat.catclient.utils;

import com.bugcat.catclient.beanInfos.CatClientInfo;
import com.bugcat.catclient.scanner.CatClientInfoFactoryBean;
import com.bugcat.catclient.spi.CatDefaultConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author bugcat
 * */
public class CatClientUtil implements ApplicationContextAware {

    
    public static final Pattern keyPat1 = Pattern.compile("^\\$\\{(.+)\\}$");
    public static final Pattern keyPat2 = Pattern.compile("^\\#\\{(.+)\\}$");
    
    
    private static Map<Class, Object> catClinetMap = new ConcurrentHashMap<>();

    private static ApplicationContext context;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
    
    
    public static <T> T getBean(Class<T> clazz){
        try {
            return context.getBean(clazz);
        } catch ( Exception e ) {
            return (T) catClinetMap.get(clazz);
        }
    }
    
    /**
     * 注册bean
     * */
    public static void registerBean(Class type, Object bean){
        catClinetMap.putIfAbsent(type, bean);
    }
    
    /**
     * 通过静态方法创建
     * */
    public static <T> T proxy(Class<T> inter){
        return proxy(inter, new Properties());
    }
    
    /**
     * 通过静态方法创建，包含读取环境变量情况
     * */
    public static <T> T proxy(Class<T> inter, Properties properties){
        if( catClinetMap.containsKey(inter) ){
            return (T) catClinetMap.get(inter);
        }
        CatDefaultConfiguration config = (CatDefaultConfiguration) properties.getOrDefault(CatDefaultConfiguration.class, getBean(CatDefaultConfiguration.class));
        if( config == null ){
            config = new CatDefaultConfiguration();
            registerBean(CatDefaultConfiguration.class, config);
        }
        ToosProperty prop = new ToosProperty(properties);
        CatClientInfo clientInfo = CatClientInfoFactoryBean.buildClientInfo(inter, prop);
        T bean = CatClientInfoFactoryBean.createCatClients(inter, clientInfo, prop);
        registerBean(inter, bean);
        return bean;
    }
    
    
    
    private static class ToosProperty extends Properties {

        private Properties prop;

        public ToosProperty(Properties prop) {
            this.prop = prop;
        }

        @Override
        public String getProperty(String key) {
            return getProperty(key, null);
        }
        @Override
        public String getProperty(String key, String defaultValue) {
            if( key.startsWith("${") ){
                Matcher matcher = keyPat1.matcher(key);
                if ( matcher.find() ) {
                    String[] keys = matcher.group(1).split(":");
                    if( keys.length > 1 && defaultValue == null ){
                        defaultValue = keys[1];
                    }
                    key = keys[0];
                }
            }
            String value = prop.getProperty(key, defaultValue);
            return value == null ? key : value;
        }
    }
    
    
}