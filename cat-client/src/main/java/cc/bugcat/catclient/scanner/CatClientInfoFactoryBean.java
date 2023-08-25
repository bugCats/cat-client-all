package cc.bugcat.catclient.scanner;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.beanInfos.CatMethodInfo;
import cc.bugcat.catclient.handler.CatClientDepend;
import cc.bugcat.catclient.handler.CatClientFactoryAdapter;
import cc.bugcat.catclient.handler.CatMethodAopInterceptor;
import cc.bugcat.catclient.spi.CatSendInterceptor;
import cc.bugcat.catclient.utils.CatClientUtil;
import cc.bugcat.catface.handler.EnvironmentAdapter;
import cc.bugcat.catface.utils.CatToosUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 生成被@CatClient标记的interface的代理对象
 *
 * @author: bugcat
 * */
public class CatClientInfoFactoryBean<T> extends AbstractFactoryBean<T> {


    private final static Log log = LogFactory.getLog(CatClientInfoFactoryBean.class);

    /**
     * 被{@code @CatClient}标记的interface
     * */
    private Class<T> interfaceClass;

    /**
     * 可以null，不一定是interface上的注解！
     * */
    private CatClient catClient;




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
            CatClientInfo clientInfo = CatClientInfo.build(interfaceClass, catClient, clientDepend);
            return createCatClient(interfaceClass, clientInfo);
        } catch ( Exception ex ) {
            log.error(buildMessage(ex));
            ((ConfigurableApplicationContext) CatClientUtil.getContext()).close();
            throw new IllegalStateException(ex);
        }
    }


    /**
     * 解析interface方法，生成动态代理类
     */
    public final static <T> T createCatClient(Class<T> interfaceClass, CatClientInfo clientInfo) {

        Class[] interfaces = new Class[]{interfaceClass};
        
        CatClientDepend clientDepend = clientInfo.getClientDepend();
        
        //判断是否存在mock功能
        Class clientMock = clientDepend.getClientMock(interfaceClass);
        if ( clientMock != null ) { //获取到mock
            clientInfo.getLogger().warn("[" + interfaceClass.getName() + "]启用mock功能");
            Enhancer enhancer = new Enhancer();
            enhancer.setInterfaces(interfaces);
            enhancer.setSuperclass(clientMock);
            enhancer.setCallback(clientDepend.getSuperObjectInterceptor());
            Object obj = enhancer.create();
            return (T) obj;
        }

        //没有mock类
        final Map<String, Method> methodMap = new HashMap<>();
        if( clientInfo.isFallbackMod() ){
            for( Method method : interfaceClass.getMethods() ){
                methodMap.put(CatToosUtil.signature(method), method);
            }
        }
        final CatClientFactoryAdapter factoryAdapter = new CatClientFactoryAdapter(clientInfo.getClientFactory());

        CatCallbackHelper helper = new CatCallbackHelper()
                .setClientInfo(clientInfo)
                .setFactoryAdapter(factoryAdapter)
                .setMethodMap(methodMap);

        CatCallbackFilter filter = new CatCallbackFilter();
        filter.parse(helper, clientInfo.getFallback(), interfaces);
        
        Enhancer enhancer = new Enhancer();
        enhancer.setInterfaces(interfaces);
        enhancer.setSuperclass(clientInfo.getFallback());
        enhancer.setCallbackFilter(filter);
        enhancer.setCallbacks(filter.getCallbacks());
        Object obj = enhancer.create();
        return (T) obj;
    }
    


    private static class CatCallbackHelper {
        
        private CatClientInfo clientInfo;
        private CatClientFactoryAdapter factoryAdapter;
        private Map<String, Method> methodMap;

        public CatCallbackHelper setClientInfo(CatClientInfo clientInfo) {
            this.clientInfo = clientInfo;
            return this;
        }
        public CatCallbackHelper setFactoryAdapter(CatClientFactoryAdapter factoryAdapter) {
            this.factoryAdapter = factoryAdapter;
            return this;
        }
        public CatCallbackHelper setMethodMap(Map<String, Method> methodMap) {
            this.methodMap = methodMap;
            return this;
        }
    }


    private static class CatCallbackFilter implements CallbackFilter {

        private Map<Method, Integer> methodIndexMap = new HashMap<>();
        private List<Callback> callbacks = new ArrayList<>();

        public void parse(CatCallbackHelper helper, Class superclass, Class[] interfaces) {

            List<Method> methods = new ArrayList(30);
            Enhancer.getMethods(superclass, interfaces, methods);
            Map<Callback, Integer> indexes = new HashMap();

            for(int index = 0, size = methods.size(); index < size; index ++ ) {
                Method method = methods.get(index); // method为增强后的方法 asm-method
                Callback callback = this.getCallback(helper, method);
                if (indexes.get(callback) == null) {
                    indexes.put(callback, index);
                }
                this.methodIndexMap.put(method, index);
                this.callbacks.add(callback);
            }
        }

        private Callback getCallback (CatCallbackHelper helper, Method method) {
            CatClientDepend clientDepend = helper.clientInfo.getClientDepend();
            CatSendInterceptor sendInterceptor = helper.clientInfo.getSendInterceptor();
            EnvironmentAdapter envProp = clientDepend.getEnvironment();

            if( CatToosUtil.isObjectMethod(method) ){//默认方法
                return clientDepend.getSuperObjectInterceptor();
            } else {

                /**
                 * 是否使用了 fallback？
                 * 如果使用了回调模式，入参Method，为fallback类中的方法
                 * 需要切换成interface上的方法
                 * */
                Method info = helper.methodMap.get(CatToosUtil.signature(method));
                if( info != null ){
                    method = info;
                }

                CatMethodInfo methodInfo = CatMethodInfo.builder(method, helper.clientInfo, envProp).build();

                CatMethodAopInterceptor interceptor = CatMethodAopInterceptor.builder()
                        .clientInfo(helper.clientInfo)
                        .methodInfo(methodInfo)
                        .method(method)
                        .factoryAdapter(helper.factoryAdapter)
                        .methodInterceptor(sendInterceptor)
                        .build();

                return interceptor; //代理方法=aop
            }
        }

        @Override
        public int accept(Method method) {
            return this.methodIndexMap.get(method);
        }

        public Callback[] getCallbacks() {
            return this.callbacks.toArray(new Callback[callbacks.size()]);
        }

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

}
