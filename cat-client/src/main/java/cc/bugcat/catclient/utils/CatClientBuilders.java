package cc.bugcat.catclient.utils;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.handler.CatClientDepend;
import cc.bugcat.catclient.scanner.CatClientInfoFactoryBean;
import cc.bugcat.catclient.spi.CatClientProvider;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 通过静态方法创建CatClient客户端对象
 * @author bugcat
 * */
public abstract class CatClientBuilders {


    /**
     * 通过interface创建
     * */
    public static <T> CatClientBuilder<T> builder(Class<T> interfaceClass){
        return CatClientBuilder.builder(interfaceClass);
    }

    /**
     * 通过CatClientProvider + interface创建
     * */
    public static <T> CatClientBuilder<T> builder(Class<? extends CatClientProvider> clientProvider, Class<T> interfaceClass){
        return CatClientBuilder.builder(clientProvider, interfaceClass);
    }

    /**
     * 通过CatClientProvider创建
     * */
    public static DefineCatClientBuilder define(Class<? extends CatClientProvider> clientProvider){
        return DefineCatClientBuilder.builder(clientProvider);
    }




    public static final class CatClientBuilder<T> {

        private Class<T> interfaceClass;
        private CatClientDepend clientDepend;
        private CatClient catClient;

        private CatClientBuilder() { }

        public static <T> CatClientBuilder<T> builder(Class<T> interfaceClass){
            CatClientBuilder<T> builder = new CatClientBuilder();
            builder.interfaceClass = interfaceClass;
            return builder;
        }

        public static <T> CatClientBuilder<T> builder(Class<? extends CatClientProvider> clientProvider, Class<T> interfaceClass){
            CatClientBuilder<T> builder = new CatClientBuilder();
            builder.interfaceClass = interfaceClass;
            for ( Method method : clientProvider.getMethods() ) {
                Class clazz = method.getReturnType();
                if( clazz.equals(interfaceClass) ){
                    builder.catClient(method.getAnnotation(CatClient.class));
                    break;
                }
            }
            return builder;
        }


        public CatClientBuilder<T> clientDepend(CatClientDepend clientDepend) {
            this.clientDepend = clientDepend;
            return this;
        }

        /**
         * @param catClient 可是通过反射获取的@CatClient注解，也可以是CatClientInstance实例
         * */
        public CatClientBuilder<T> catClient(CatClient catClient){
            this.catClient = catClient;
            return this;
        }

        public <T> T build(){
            if( catClient == null ){
                catClient = interfaceClass.getAnnotation(CatClient.class);
            }
            T bean = (T) buildClinet(interfaceClass, catClient, clientDepend);
            return bean;
        }
    }


    public static final class DefineCatClientBuilder {

        private Class<? extends CatClientProvider> clientProvider;
        private CatClientDepend clientDepend;
        private Predicate<Class> filter;

        public DefineCatClientBuilder() { }

        public static DefineCatClientBuilder builder(Class<? extends CatClientProvider> clientProvider){
            DefineCatClientBuilder builder = new DefineCatClientBuilder();
            builder.clientProvider = clientProvider;
            return builder;
        }

        public DefineCatClientBuilder clientDepend(CatClientDepend clientDepend) {
            this.clientDepend = clientDepend;
            return this;
        }

        public DefineCatClientBuilder filter(Predicate<Class> filter){
            this.filter = filter;
            return this;
        }

        public Map<Class, Object> build(){
            if( filter == null ){
                filter = clazz -> Boolean.TRUE;
            }

            Map<Class, Object> clientMap = new HashMap<>();
            Method[] methods = clientProvider.getMethods();
            for ( Method method : methods ) {
                CatClient catClient = method.getAnnotation(CatClient.class);
                if( catClient == null ){
                    continue;
                }
                Class clazz = method.getReturnType();
                if( filter.test(clazz) ){
                    Object value = buildClinet(clazz, catClient, clientDepend);
                    clientMap.put(clazz, value);
                }
            }
            return clientMap;
        }
    }


    private static Object buildClinet(Class interfaceClass, CatClient catClient, CatClientDepend clientDepend){
        if( CatClientUtil.contains(interfaceClass) ){
            return CatClientUtil.getBean(interfaceClass);
        }
        if( clientDepend == null ){
            clientDepend = CatClientUtil.getBean(CatClientDepend.class);
            if( clientDepend == null ){
                clientDepend = CatClientDepend.builder().build();
            }
        }
        CatClientInfo clientInfo = CatClientInfo.build(interfaceClass, catClient, clientDepend);
        Object bean = CatClientInfoFactoryBean.createCatClient(interfaceClass, clientInfo);
        CatClientUtil.registerBean(interfaceClass, bean);
        return bean;
    }


}
