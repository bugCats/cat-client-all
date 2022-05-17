package cc.bugcat.catclient.utils;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.spi.DefineCatClients;
import cc.bugcat.catclient.handler.CatClientDepend;
import cc.bugcat.catclient.scanner.CatClientInfoFactoryBean;
import cc.bugcat.catface.utils.CatToosUtil;
import org.springframework.core.env.Environment;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
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
     * 通过DefineCatClients + interface创建
     * */
    public static <T> CatClientBuilder<T> builder(Class<? extends DefineCatClients> defineClients, Class<T> interfaceClass){
        return CatClientBuilder.builder(defineClients, interfaceClass);
    }

    /**
     * 通过DefineCatClients创建
     * */
    public static DefineCatClientBuilder define(Class<? extends DefineCatClients> defineClients){
        return DefineCatClientBuilder.builder(defineClients);
    }




    public static final class CatClientBuilder<T> {

        private Class<T> interfaceClass;
        private Properties properties;
        private CatClientDepend clientDepend;
        private CatClient catClient;

        private CatClientBuilder() { }

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

        /**
         * 环境配置
         * @see CatToosUtil#envProperty(Environment) 将spring环境变量适配为Properties
         * 
         * @param properties 可以是普通Properties，也可以通过CatClientUtil#envProperty(Environment)适配
         * */
        public CatClientBuilder<T> environment(Properties properties){
            this.properties = properties;
            return this;
        }

        public CatClientBuilder<T> clientDepend(CatClientDepend clientDepend) {
            this.clientDepend = clientDepend;
            return this;
        }

        /**
         * @param catClient 可是通过反射获取的@CatClient注解，也可以是CatClients实例
         * */
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

            T bean = (T) buildClinet(interfaceClass, catClient, clientDepend, properties);
            return bean;
        }
    }


    public static final class DefineCatClientBuilder {

        private Class<? extends DefineCatClients> defineClients;
        private Properties properties;
        private CatClientDepend clientDepend;
        private Predicate<Class> filter;

        public DefineCatClientBuilder() { }

        public static DefineCatClientBuilder builder(Class<? extends DefineCatClients> defineClients){
            DefineCatClientBuilder builder = new DefineCatClientBuilder();
            builder.defineClients = defineClients;
            return builder;
        }

        /**
         * 环境配置
         * @see CatToosUtil#envProperty(Environment) 将spring环境变量适配为Properties
         *
         * @param properties 可以是普通Properties，也可以通过CatClientUtil#envProperty(Environment)适配
         * */
        public DefineCatClientBuilder environment(Properties properties){
            this.properties = properties;
            return this;
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
                    Object value = buildClinet(clazz, catClient, clientDepend, properties);
                    clientMap.put(clazz, value);
                }
            }
            return clientMap;
        }
    }


    private static Object buildClinet(Class interfaceClass, CatClient catClient, CatClientDepend clientDepend, Properties properties){
        if( CatClientUtil.contains(interfaceClass) ){
            return CatClientUtil.getBean(interfaceClass);
        }
        Properties envProp = CatToosUtil.envProperty(properties);
        if( clientDepend == null ){
            clientDepend = CatClientUtil.getBean(CatClientDepend.class);
            if( clientDepend == null ){
                clientDepend = CatClientDepend.builder().build();
            }
        }
        CatClientInfo clientInfo = CatClientInfo.build(interfaceClass, catClient, clientDepend, envProp);
        Object bean = CatClientInfoFactoryBean.createCatClient(interfaceClass, clientInfo, envProp);
        CatClientUtil.registerBean(interfaceClass, bean);
        return bean;
    }


}
