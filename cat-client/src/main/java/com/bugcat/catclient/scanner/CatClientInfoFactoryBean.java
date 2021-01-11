package com.bugcat.catclient.scanner;

import com.bugcat.catclient.annotation.CatClient;
import com.bugcat.catclient.annotation.CatMethod;
import com.bugcat.catclient.beanInfos.CatClientInfo;
import com.bugcat.catclient.beanInfos.CatMethodInfo;
import com.bugcat.catclient.handler.CatMethodInterceptor;
import com.bugcat.catclient.spi.CatClientFactory;
import com.bugcat.catclient.spi.DefaultConfiguration;
import com.bugcat.catclient.utils.CatClientUtil;
import com.bugcat.catface.utils.CatToosUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.cglib.proxy.CallbackHelper;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;



/**
 * 生成被@CatClient标记的interface的代理对象
 * @author: bugcat
 * */
public class CatClientInfoFactoryBean<T> extends AbstractFactoryBean<T> {
    
    // interface的class
    private Class<T> clazz;
    
    
    // 
    private CatClient client;
    
    
    private Properties prop;

    @Override
    public Class<?> getObjectType() {
        return clazz;
    }


    @Override
    protected T createInstance() throws Exception {
        CatClientInfo clientInfo = CatClientInfo.build(clazz, client, prop);
        return createCatClient(clazz, clientInfo, prop);
    }

    /**
     * 解析interface方法，生成动态代理类
     */
    public final static <T> T createCatClient(Class<T> clazz, CatClientInfo clientInfo, Properties prop) {
        
        Map<String, Map<String, Method>> methodMap = new HashMap<>();
        
        /**
         * 是否使用了 fallback
         * 如果使用了，必须将interface中的方法，与fallback类中方法关联起来
         * */
        if( clientInfo.isFallbackMod() ){
            Method[] methods = clazz.getDeclaredMethods();
            for( Method method : methods ){
                CatMethod catMethod = method.getAnnotation(CatMethod.class);
                if ( catMethod != null ) {
                    Map<String, Method> map = methodMap.get(method.getName());
                    if ( map == null ) {
                        map = new HashMap<>();
                        methodMap.put(method.getName(), map);
                    }
                    map.put(CatToosUtil.signature(method), method);
                }
            }
        }
        
        Class[] interfaces = new Class[]{clazz};

        CallbackHelper helper = new CallbackHelper(clientInfo.getFallback(), interfaces) {

            @Override
            protected Object getCallback (Method method) {

                if( clientInfo.isFallbackMod() ){
                    Map<String, Method> map = methodMap.get(method.getName());
                    if( map != null ){
                        Method tmp = map.get(CatToosUtil.signature(method));
                        if( tmp != null ){
                            method = tmp;
                        }
                    }
                }

                StandardMethodMetadata metadata = new StandardMethodMetadata(method);
                Map<String, Object> attr = metadata.getAnnotationAttributes(CatMethod.class.getName());
                if ( attr != null ) {  //如果方法上有 CatMethod
                    
                    CatMethodInfo methodInfo = new CatMethodInfo(method, clientInfo, prop);
                    
                    CatClientMethodInterceptor interceptor = new CatClientMethodInterceptor(clientInfo, methodInfo);//代理方法=aop
                    CatClientUtil.addInitBean(interceptor);
                    
                    return interceptor;//代理方法=aop

                } else {
                    return new MethodInterceptor() {    //默认方法
                        @Override
                        public Object intercept (Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                            return methodProxy.invokeSuper(target, args);
                        }
                    };
                }
            }
        };

        Enhancer enhancer = new Enhancer();
        enhancer.setInterfaces(interfaces);
        enhancer.setSuperclass(clientInfo.getFallback());
        enhancer.setCallbackFilter(helper);
        enhancer.setCallbacks(helper.getCallbacks());
        Object obj = enhancer.create();
        return (T) obj;
    }

    
    /**
     * 动态代理 方法拦截器
     * */
    private static final class CatClientMethodInterceptor implements MethodInterceptor, InitializingBean {

        private CatClientInfo catClientInfo;
        private CatMethodInfo methodInfo;
        private CatMethodInterceptor interceptor;

        public CatClientMethodInterceptor(CatClientInfo catClientInfo, CatMethodInfo methodInfo){
            this.catClientInfo = catClientInfo;
            this.methodInfo = methodInfo;
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            interceptor = CatClientUtil.getBean(catClientInfo.getInterceptor());
            
            DefaultConfiguration config = CatClientUtil.getBean(DefaultConfiguration.class);
            CatClientFactory factory = CatClientUtil.getBean(catClientInfo.getFactoryClass());
            factory.configuration(config);
            methodInfo.setFactory(factory);
        }

        @Override
        public Object intercept(Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            return interceptor.intercept(catClientInfo, methodInfo, target, method, args, methodProxy);
        }
    }


    /***************************这些属性通过IOC注入进来，因此get set方法不能少*********************************/


    public Class<T> getClazz() {
        return clazz;
    }
    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    public CatClient getClient() {
        return client;
    }
    public void setClient(CatClient client) {
        this.client = client;
    }

    public Properties getProp() {
        return prop;
    }
    public void setProp(Properties prop) {
        this.prop = prop;
    }
}
