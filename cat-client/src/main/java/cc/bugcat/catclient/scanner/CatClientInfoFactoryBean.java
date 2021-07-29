package cc.bugcat.catclient.scanner;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.beanInfos.CatMethodInfo;
import cc.bugcat.catclient.beanInfos.CatMethodInfoBuilder;
import cc.bugcat.catclient.handler.CatMethodInterceptor;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.DefaultConfiguration;
import cc.bugcat.catclient.utils.CatClientUtil;
import cc.bugcat.catface.utils.CatToosUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.cglib.proxy.CallbackHelper;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * 生成被@CatClient标记的interface的代理对象
 * @author: bugcat
 * */
public class CatClientInfoFactoryBean<T> extends AbstractFactoryBean<T> {
    
    
    private Class<T> clazz;     // interface的class
    private CatClient client;   // 可以null，不一定是interface上的注解！
    private Properties prop;    // 环境变量
    
    
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
        
        Class[] interfaces = new Class[]{clazz};

        Map<String, Method> methodMap = new HashMap<>();
        if( clientInfo.isFallbackMod() ){
            for( Method method : clazz.getMethods() ){
                methodMap.put(CatToosUtil.signature(method), method);
            }
        }
   
        
        CallbackHelper helper = new CallbackHelper(clientInfo.getFallback(), interfaces) {

            @Override
            protected Object getCallback (Method method) {
                if( CatToosUtil.isObjectMethod(method) ){//默认方法
                    return new MethodInterceptor() {   
                        @Override
                        public Object intercept (Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                            return methodProxy.invokeSuper(target, args);
                        }
                    };
                } else {
                    
                    /**
                     * 是否使用了 fallback
                     * 如果使用了，CallbackHelper.getCallback的入参Method，为fallback类中方法
                     * 需要切换成interface上的方法
                     * */
                    Method info = methodMap.get(CatToosUtil.signature(method));
                    if( info != null ){
                        method = info;
                    }

                    CatMethodInfoBuilder builder = CatMethodInfoBuilder.builder(method, clientInfo, prop);
                    CatMethodInfo methodInfo = builder.build();
                    
                    CatClientMethodInterceptor interceptor = new CatClientMethodInterceptor(clientInfo, methodInfo);//代理方法=aop
                    CatClientUtil.addInitBean(interceptor);
                    
                    return interceptor; //代理方法=aop
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

        private CatClientInfo clientInfo;
        private CatMethodInfo methodInfo;
        private CatMethodInterceptor interceptor;

        public CatClientMethodInterceptor(CatClientInfo clientInfo, CatMethodInfo methodInfo){
            this.clientInfo = clientInfo;
            this.methodInfo = methodInfo;
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            interceptor = CatClientUtil.getBean(clientInfo.getInterceptor());
            
            DefaultConfiguration config = CatClientUtil.getBean(DefaultConfiguration.class);
            CatClientFactory factory = CatClientUtil.getBean(clientInfo.getFactoryClass());
            factory.configuration(config);
            methodInfo.setClientFactory(factory);
        }

        @Override
        public Object intercept(Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            return interceptor.intercept(clientInfo, methodInfo, target, method, args, methodProxy);
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
