package com.bugcat.catclient.utils;

import com.bugcat.catclient.annotation.CatClient;
import com.bugcat.catclient.beanInfos.CatClientInfo;
import com.bugcat.catclient.config.CatHttpRetryConfigurer;
import com.bugcat.catclient.handler.CatClients;
import com.bugcat.catclient.scanner.CatClientInfoFactoryBean;
import com.bugcat.catclient.spi.CatClientFactory;
import com.bugcat.catclient.spi.CatHttp;
import com.bugcat.catclient.spi.DefaultConfiguration;
import com.bugcat.catclient.spi.DefaultMethodInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author bugcat
 * */
@ComponentScan("com.bugcat.catclient")
public class CatClientUtil implements ApplicationContextAware, DisposableBean {

    
    public static final Pattern keyPat1 = Pattern.compile("^\\$\\{(.+)\\}$");
    public static final Pattern keyPat2 = Pattern.compile("^\\#\\{(.+)\\}$");
    
    
    private static Map<Class, Object> catClinetMap = new ConcurrentHashMap<>(); //自定义组件容器
    private static ApplicationContext context;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
    
    @Override
    public void destroy() throws Exception {
        Inner.run = false;
    }
    
    
    /**
     * 优先从Spring容器中获取
     * 其次catClinetMap
     * */
    public static <T> T getBean(Class<T> clazz){
        try {
            return context.getBean(clazz);
        } catch ( Exception e ) {
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
                throw new NoSuchBeanDefinitionException(clazz);
            } catch ( Exception ex ) {
                return (T) catClinetMap.get(clazz);
            }
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

    
    /**
     * 手动注册一些非Spring组件的初始化
     * */
    public static final void addInitBean(InitializingBean bean){
        BeanInitHandler.beans.add(bean);
    }




    /**
     * 通过静态方法创建
     * */
    public static <T> T proxy(Class<T> inter){
        return proxy(inter, envProperty());
    }
    
    /**
     * 通过静态方法创建，包含读取环境变量情况
     * */
    public static <T> T proxy(Class<T> inter, Properties properties){
        return proxy(inter, inter.getAnnotation(CatClient.class), properties);
    }
    
    /**
     * 通过CatClients创建
     * */
    public static <T> T proxy(Class<? extends CatClients> clients, Class<T> inter, Properties properties){
        Map<Class, Object> clientMap = proxys(clients, clazz -> clazz.equals(inter), properties);
        return (T) clientMap.get(inter);
    }
    
    /**
     * 通过CatClients创建
     * */
    public static Map<Class, Object> proxys(Class<? extends CatClients> catClients, Properties properties){
        return proxys(catClients, clazz -> Boolean.TRUE, properties);
    }

    private static Map<Class, Object> proxys(Class<? extends CatClients> clients, Predicate<Class> filter, Properties properties){
        Map<Class, Object> clientMap = new HashMap<>();
        Method[] methods = clients.getMethods();
        for ( Method method : methods ) {
            CatClient client = method.getAnnotation(CatClient.class);
            if( client == null ){
                continue;
            }
            Class clazz = method.getReturnType();
            if( filter.test(clazz) ){
                Object value = proxy(clazz, client, properties);
                clientMap.put(clazz, value);
            }
        }
        return clientMap;
    }
    
    private final static <T> T proxy(Class<T> inter, CatClient client, Properties properties){
        if( catClinetMap.containsKey(inter) ){
            return (T) catClinetMap.get(inter);
        }
        Inner.noop();
        DefaultConfiguration config = (DefaultConfiguration) properties.get(DefaultConfiguration.class);
        if( config != null ){
            refreshBean(DefaultConfiguration.class, config);
        }
        ToosProperty prop = new ToosProperty(properties);
        CatClientInfo clientInfo = CatClientInfo.build(inter, client, prop);
        T bean = CatClientInfoFactoryBean.createCatClient(inter, clientInfo, prop);
        registerBean(inter, bean);
        return bean;
    }
    
    
    
    
    public static Properties envProperty(){
        return context != null ? new EnvironmentProperty(context.getEnvironment()) : new Properties();
    }
    
    
    public static Properties envProperty(Environment environment){
        return new EnvironmentProperty(environment);
    }
    
    
    
    /**
     * 自定义环境参数
     * */
    private static class ToosProperty extends Properties {

        private Properties prop;

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
     * 环境参数
     * */
    private static class EnvironmentProperty extends Properties {

        private Environment environment;

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

    
    /**
     * 如果使用main方法执行，需要初始化加载一些bean
     * */
    private static class Inner {
        
        private static boolean run = true;
        
        static {
            
            if( context == null ){
                
                DefaultConfiguration config = new DefaultConfiguration();
                registerBean(DefaultConfiguration.class, config);
                
                CatClientFactory factory = new CatClientFactory();
                registerBean(CatClientFactory.class, factory);
                
                DefaultMethodInterceptor interceptor = new DefaultMethodInterceptor();
                registerBean(DefaultMethodInterceptor.class, interceptor);
                
                try {
                    
                    CatHttpRetryConfigurer retry = new CatHttpRetryConfigurer();
                    retry.init();
                    registerBean(CatHttpRetryConfigurer.class, retry);
                    
                    Class<?> clazz = Class.forName("com.bugcat.catclient.utils.CatHttpUtil");
                    CatHttp http = (CatHttp) clazz.newInstance();
                    registerBean(CatHttp.class, http);
                    
                } catch ( Exception ex ) {
                    System.err.println(ex.getMessage());
                }
                
                Thread worker = new Thread(() -> {
                    BlockingQueue<InitializingBean> beans = BeanInitHandler.beans;
                    while ( run ) {
                        try {
                            InitializingBean bean = beans.poll(60, TimeUnit.SECONDS);
                            if( bean == null ){
                                run = false;
                                break;
                            }
                            bean.afterPropertiesSet();
                        } catch ( Exception ex ) {
                            run = false;
                        }
                    }
                });
                worker.start();
            }
        }
        
        private static final void noop(){}
    }
    
    
    /**
     * 在所有的组件初始后执行
     * */
    @Order
    @Configuration
    public static class BeanInitHandler implements InitializingBean {
        
        private static BlockingQueue<InitializingBean> beans = new LinkedBlockingQueue<>();

        @Override
        public void afterPropertiesSet() throws Exception {
            doInitBean();
        }

        public static final void doInitBean() {
            while ( true ) {
                try {
                    InitializingBean bean = beans.poll();
                    if( bean == null ){
                        break;
                    }
                    bean.afterPropertiesSet();
                } catch ( Exception ex ) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }




}