package cc.bugcat.catclient.scanner;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.beanInfos.CatMethodInfo;
import cc.bugcat.catclient.config.CatHttpRetryConfigurer;
import cc.bugcat.catclient.handler.CatClientDepend;
import cc.bugcat.catclient.handler.CatClientFactoryAdapter;
import cc.bugcat.catclient.handler.CatMethodAopInterceptor;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatMethodSendInterceptor;
import cc.bugcat.catclient.utils.CatClientUtil;
import cc.bugcat.catface.utils.CatToosUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.cglib.proxy.CallbackHelper;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;


/**
 * 生成被@CatClient标记的interface的代理对象
 *
 * @author: bugcat
 * */
public class CatClientInfoFactoryBean<T> extends AbstractFactoryBean<T> {


    private final Log log = LogFactory.getLog(CatClientInfoFactoryBean.class);

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
    public boolean isSingleton() {
        return true;
    }

    @Override
    public Class<T> getObjectType() {
        return interfaceClass;
    }


    @Override
    protected T createInstance() throws Exception {
        try {
            CatClientDepend clientDepend = CatClientUtil.getBean(CatClientDepend.class);
            CatClientInfo clientInfo = CatClientInfo.build(interfaceClass, catClient, clientDepend, envProp);
            return createCatClient(interfaceClass, clientInfo, envProp);
        } catch ( Exception ex ) {
            log.error(buildMessage(ex));
            ((ConfigurableApplicationContext) CatClientUtil.getContext()).close();
            throw new IllegalStateException(ex);
        }
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

        CatClientDepend clientDepend = clientInfo.getClientDepend();

        CatClientFactory clientFactory = getAndExectue((Class<CatClientFactory>)clientInfo.getFactoryClass(), bean -> {
            if( bean == null ){
                bean = clientDepend.getDefaultClientFactory();
            }
            bean.setClientConfiguration(clientDepend.getClientConfig());
            return bean;
        });

        final MethodInterceptor defaultInterceptor = clientDepend.getDefaultInterceptor();
        final CatHttpRetryConfigurer retryConfigurer = clientDepend.getRetryConfigurer();
        final CatMethodSendInterceptor methodInterceptor = getAndExectue((Class<CatMethodSendInterceptor>) clientInfo.getInterceptorClass(), bean -> {
            if( bean == null ){
                bean = clientDepend.getDefaultSendInterceptor();
            }
            return bean;
        });
        final CatClientFactoryAdapter factoryAdapter = new CatClientFactoryAdapter(clientFactory);

        CallbackHelper helper = new CallbackHelper(clientInfo.getFallback(), interfaces) {

            @Override
            protected Object getCallback (Method method) {
                if( CatToosUtil.isObjectMethod(method) ){//默认方法
                    return defaultInterceptor;
                } else {

                    /**
                     * 是否使用了 fallback？
                     * 如果使用了回调模式，入参Method，为fallback类中的方法
                     * 需要切换成interface上的方法
                     * */
                    Method info = methodMap.get(CatToosUtil.signature(method));
                    if( info != null ){
                        method = info;
                    }

                    CatMethodInfo methodInfo = CatMethodInfo.builder(method, clientInfo, envprop).build();

                    CatMethodAopInterceptor interceptor = CatMethodAopInterceptor.builder()
                            .clientInfo(clientInfo)
                            .methodInfo(methodInfo)
                            .method(method)
                            .factoryAdapter(factoryAdapter)
                            .methodInterceptor(methodInterceptor)
                            .retryConfigurer(retryConfigurer)
                            .build();

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



    private static <T> T getAndExectue(Class<T> clazz, Function<T, T> exectue){
        T bean = CatClientUtil.getBean(clazz);
        return exectue.apply(bean);
    }


    private String buildMessage(Exception exception) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("CatClient-interface creation failed: %s%n", interfaceClass.getName()));
        builder.append(String.format("%s: %s%n", exception.getClass().getName(), exception.getMessage()));
        StackTraceElement[] stackTraces = CatToosUtil.filterStackTrace(exception, CatToosUtil.GROUP_ID);
        for ( StackTraceElement stackTrace : stackTraces ) {
            builder.append(String.format("    at %s:%n", stackTrace.toString()));
        }
        return builder.toString();
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
