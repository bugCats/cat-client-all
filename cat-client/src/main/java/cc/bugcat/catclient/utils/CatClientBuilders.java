package cc.bugcat.catclient.utils;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.config.CatHttpRetryConfigurer;
import cc.bugcat.catclient.handler.CatMethodAopInterceptor;
import cc.bugcat.catclient.handler.DefineCatClients;
import cc.bugcat.catclient.scanner.CatClientInfoFactoryBean;
import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatHttp;
import cc.bugcat.catclient.spi.CatMethodInterceptor;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public abstract class CatClientBuilders {


    public static <T> CatClientBuilder<T> builder(Class<T> interfaceClass){
        return CatClientBuilder.builder(interfaceClass);
    }

    public static <T> CatClientBuilder<T> builder(Class<? extends DefineCatClients> defineClients, Class<T> interfaceClass){
        return CatClientBuilder.builder(interfaceClass);
    }

    public static DefineCatClientBuilder define(Class<? extends DefineCatClients> defineClients){
        return DefineCatClientBuilder.builder(defineClients);
    }





    public static final class CatClientBuilder<T> {

        private Class<T> interfaceClass;
        private Properties properties;
        private CatClient catClient;

        static {
            Inner.noop();
        }
        private CatClientBuilder() { Inner.noop(); }

        public static <T> CatClientBuilder<T> builder(Class<T> interfaceClass){
            CatClientBuilder<T> builder = new CatClientBuilder();
            builder.interfaceClass = interfaceClass;
            return builder;
        }

        public static <T> CatClientBuilder<T> builder(Class<? extends DefineCatClients> defineClients, Class<T> interfaceClass){
            CatClientBuilder<T> builder = new CatClientBuilder();
            builder.interfaceClass = interfaceClass;
            for ( Method method : defineClients.getMethods() ) {
                Class clazz = method.getReturnType();
                if( clazz.equals(interfaceClass) ){
                    builder.catClient(method.getAnnotation(CatClient.class));
                    break;
                }
            }
            return builder;
        }

        public CatClientBuilder<T> environment(Properties properties){
            this.properties = properties;
            return this;
        }

        public CatClientBuilder<T> catClient(CatClient catClient){
            this.catClient = catClient;
            return this;
        }

        public <T> T build(){
            if( properties == null ){
                properties = new Properties();
            }
            if( catClient == null ){
                catClient = interfaceClass.getAnnotation(CatClient.class);
            }

            T bean = (T) buildClinet(interfaceClass, catClient, properties);
            return bean;
        }
    }


    public static final class DefineCatClientBuilder {

        private Class<? extends DefineCatClients> defineClients;
        private Properties properties;
        private Predicate<Class> filter;

        static {
            Inner.noop();
        }
        public DefineCatClientBuilder() { }

        public static DefineCatClientBuilder builder(Class<? extends DefineCatClients> defineClients){
            DefineCatClientBuilder builder = new DefineCatClientBuilder();
            builder.defineClients = defineClients;
            return builder;
        }
        public DefineCatClientBuilder environment(Properties properties){
            this.properties = properties;
            return this;
        }
        public DefineCatClientBuilder filter(Predicate<Class> filter){
            this.filter = filter;
            return this;
        }

        public Map<Class, Object> build(){
            if( properties == null ){
                properties = new Properties();
            }
            if( filter == null ){
                filter = clazz -> Boolean.TRUE;
            }

            Map<Class, Object> clientMap = new HashMap<>();
            Method[] methods = defineClients.getMethods();
            for ( Method method : methods ) {
                CatClient catClient = method.getAnnotation(CatClient.class);
                if( catClient == null ){
                    continue;
                }
                Class clazz = method.getReturnType();
                if( filter.test(clazz) ){
                    Object value = buildClinet(clazz, catClient, properties);
                    clientMap.put(clazz, value);
                }
            }
            return clientMap;
        }
    }


    private static Object buildClinet(Class interfaceClass, CatClient catClient, Properties properties){
        if( CatClientUtil.contains(interfaceClass) ){
            return CatClientUtil.getBean(interfaceClass);
        }
        Properties envProp = CatClientUtil.envProperty(properties);
        CatClientConfiguration config = CatClientUtil.getBean(CatClientConfiguration.class);
        if( config != null && CatClientUtil.notContains(config.getClass())){
            CatClientUtil.refreshBean(config.getClass(), config);
        }
        CatClientInfo clientInfo = CatClientInfo.build(interfaceClass, catClient, config, envProp);
        Object bean = CatClientInfoFactoryBean.createCatClient(interfaceClass, clientInfo, envProp);
        CatClientUtil.registerBean(interfaceClass, bean);
        return bean;
    }



    /**
     * 如果使用main方法执行，需要初始化加载一些bean
     * */
    private static class Inner {

        static {

            if( CatClientUtil.getApplicationContext() == null ){

                CatClientConfiguration config = new CatClientConfiguration();
                CatClientUtil.registerBean(CatClientConfiguration.class, config);

                CatHttp http = config.catHttp();
                CatClientUtil.registerBean(CatHttp.class, http);

                CatClientFactory factory = CatClientFactory.defaultFactory();
                factory.setClientConfiguration(config);
                CatClientUtil.registerBean(CatClientFactory.class, factory);

                CatMethodInterceptor interceptor = new CatMethodInterceptor.Default();
                CatClientUtil.registerBean(CatMethodInterceptor.class, interceptor);

                CatHttpRetryConfigurer retry = new CatHttpRetryConfigurer();
                try { retry.afterPropertiesSet(); } catch ( Exception e ) { }
                CatClientUtil.registerBean(CatHttpRetryConfigurer.class, retry);
            }
        }

        private static final void noop(){}
    }



}
