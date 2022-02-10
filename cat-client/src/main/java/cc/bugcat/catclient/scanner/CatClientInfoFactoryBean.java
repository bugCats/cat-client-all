package cc.bugcat.catclient.scanner;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.beanInfos.CatMethodInfo;
import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.config.CatHttpRetryConfigurer;
import cc.bugcat.catclient.handler.CatMethodAopInterceptor;
import cc.bugcat.catclient.utils.CatClientUtil;
import cc.bugcat.catface.utils.CatToosUtil;
import org.springframework.beans.factory.BeanFactory;
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
 *
 * @author: bugcat
 * */
public class CatClientInfoFactoryBean<T> extends AbstractFactoryBean<T> {

    /**
     * 被{@code @CatClient}标记的interface
     * */
    private Class<T> interfaceClass;

    /**
     * 可以null，不一定是interface上的注解！
     * */
    private CatClient catClient;

    /**
     * 环境变量
     * */
    private Properties envProp;



    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }



    @Override
    protected T createInstance() throws Exception {
        CatClientConfiguration clientConfig = CatClientUtil.getBean(CatClientConfiguration.class);
        CatClientInfo clientInfo = CatClientInfo.build(interfaceClass, catClient, clientConfig, envProp);
        return createCatClient(interfaceClass, clientInfo, envProp);
    }


    /**
     * 解析interface方法，生成动态代理类
     */
    public final static <T> T createCatClient(Class<T> interfaceClass, CatClientInfo clientInfo, Properties envprop) {

        Class[] interfaces = new Class[]{interfaceClass};

        Map<String, Method> methodMap = new HashMap<>();
        if( clientInfo.isFallbackMod() ){
            for( Method method : interfaceClass.getMethods() ){
                methodMap.put(CatToosUtil.signature(method), method);
            }
        }

        final MethodInterceptor defaultInterceptor = new MethodInterceptor() {
            @Override
            public Object intercept (Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                return methodProxy.invokeSuper(target, args);
            }
        };


        CallbackHelper helper = new CallbackHelper(clientInfo.getFallback(), interfaces) {

            @Override
            protected Object getCallback (Method method) {
                if( CatToosUtil.isObjectMethod(method) ){//默认方法
                    return defaultInterceptor;
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

                    CatMethodInfo methodInfo = CatMethodInfo.builder(method, clientInfo, envprop).build();

                    CatMethodAopInterceptor interceptor = new CatMethodAopInterceptor(clientInfo, methodInfo);//代理方法=aop

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




    /***************************这些属性通过IOC注入进来，因此get set方法不能少*********************************/


    public Class<T> getInterfaceClass() {
        return interfaceClass;
    }
    public void setInterfaceClass(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public CatClient getCatClient() {
        return catClient;
    }
    public void setCatClient(CatClient catClient) {
        this.catClient = catClient;
    }

    public Properties getEnvProp() {
        return envProp;
    }
    public void setEnvProp(Properties envProp) {
        this.envProp = envProp;
    }
}
