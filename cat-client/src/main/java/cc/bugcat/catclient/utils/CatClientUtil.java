package cc.bugcat.catclient.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author bugcat
 * */
@ComponentScan("cc.bugcat.catclient")
public class CatClientUtil implements ApplicationContextAware {


    public static final Pattern keyPat1 = Pattern.compile("^\\$\\{(.+)\\}$");
    public static final Pattern keyPat2 = Pattern.compile("^\\#\\{(.+)\\}$");

    private static Map<Class, Object> catClinetMap = new ConcurrentHashMap<>(); //自定义组件容器
    private static ApplicationContext context;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }


    public static ApplicationContext getApplicationContext() {
        return context;
    }


    /**
     * 优先从Spring容器中获取
     * 其次catClinetMap
     * 都没有则返回null
     * */
    public static <T> T getBean(Class<T> clazz){
        try {
            return context.getBean(clazz);
        } catch ( Exception ex ) {
            try {
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
            } catch ( Exception e ) {

            }
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
     * 刷新bean
     * */
    public static void refreshBean(Class type, Object bean){
        catClinetMap.put(type, bean);
    }


    public static boolean contains(Class key) {
        return catClinetMap.containsKey(key);
    }
    public static boolean notContains(Class key) {
        return !contains(key);
    }



    public static Properties envProperty(){
        return context != null ? new EnvironmentProperty(context.getEnvironment()) : new Properties();
    }


    public static Properties envProperty(Environment environment){
        return new EnvironmentProperty(environment);
    }

    public static Properties envProperty(Properties properties){
        return properties instanceof ToosProperty ? properties : new ToosProperty(properties);
    }



    /**
     * 自定义环境参数
     * */
    private static class ToosProperty extends Properties {

        private final Properties prop;
        private ToosProperty(Properties prop) {
            this.prop = prop;
        }

        /**
         * key 类似于 ${demo.remoteApi}
         * */
        @Override
        public String getProperty(String key) {
            return getProperty(key, null);
        }

        @Override
        public String getProperty(String key, String defaultValue) {
            if( key.startsWith("${") ){
                Matcher matcher = keyPat1.matcher(key);
                if ( matcher.find() ) {
                    String[] keys = matcher.group(1).split(":");    //例如：${value:def}，defaultValue优先度高于def
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

    /**
     * spring环境参数
     * */
    private static class EnvironmentProperty extends Properties {

        private final Environment environment;
        private EnvironmentProperty(Environment environment) {
            this.environment = environment;
        }

        /**
         * key 类似于 ${demo.remoteApi}
         * */
        @Override
        public String getProperty(String key) {
            return environment.resolvePlaceholders(key);
        }

        @Override
        public String getProperty(String key, String defaultValue) {
            String value = environment.resolvePlaceholders(key);
            return defaultValue != null && key.equals(value) ? defaultValue : value;
        }
    }







}