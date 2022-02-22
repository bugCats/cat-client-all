package cc.bugcat.catserver.handler;

import cc.bugcat.catserver.beanInfos.CatServerInfo;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.spi.CatInterceptor;
import cc.bugcat.catserver.spi.CatInterceptorGroup;
import cc.bugcat.catserver.spi.CatParameterResolver;
import cc.bugcat.catserver.spi.CatServerInterceptor;
import cc.bugcat.catserver.utils.CatServerUtil;
import jdk.internal.org.objectweb.asm.Type;
import org.springframework.cglib.core.Signature;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 方法描述信息
 *
 * @author bugcat
 * */
public class CatMethodInfo {

    /**
     * 原interface的方法
     * */
    private final StandardMethodMetadata interMethod;

    /**
     * 原server类的方法
     * */
    private final Method serverMethod;
    /**
     * controller快速调用server对象方法的
     * */
    private final ServiceMethodProxy serviceMethodProxy;

    /**
     * 全局拦截器，用来替换{@code @CatServer}上配置的默认拦截器
     * */
    private final CatInterceptor globalInterceptor;

    /**
     * {@code @CatServer}上配置的拦截器
     * */
    private final List<CatServerInterceptor> interceptors;

    /**
     * 拦截器组。
     * 运行时可动态添加移除的拦截器
     * */
    private final List<CatInterceptorGroup> interceptorGroups;

    /**
     * 精简模式下参数预处理器
     * */
    private final CatParameterResolver parameterResolver;


    protected CatMethodInfo(Builder builder){
        this.interMethod = builder.interMethod;
        this.serverMethod = builder.serverMethod;
        this.serviceMethodProxy = builder.serviceMethodProxy;
        this.globalInterceptor = builder.globalInterceptor;
        this.interceptors = builder.interceptors;
        this.interceptorGroups = builder.interceptorGroups;
        this.parameterResolver = builder.parameterResolver;
    }

    public StandardMethodMetadata getInterMethod() {
        return interMethod;
    }
    public Method getServerMethod() {
        return serverMethod;
    }
    public ServiceMethodProxy getServiceMethodProxy() {
        return serviceMethodProxy;
    }
    public CatInterceptor getGlobalInterceptor() {
        return globalInterceptor;
    }
    public List<CatServerInterceptor> getInterceptors() {
        return interceptors;
    }
    public List<CatInterceptorGroup> getInterceptorGroups() {
        return interceptorGroups;
    }
    public CatParameterResolver getParameterResolver() {
        return parameterResolver;
    }




    protected static Builder builder(){
        return new Builder();
    }

    protected static class Builder {

        /**
         * 原interface的方法
         * */
        private StandardMethodMetadata interMethod;

        /**
         * 原server类的方法
         * */
        private Method serverMethod;

        /**
         * controller快速调用server对象方法的
         * */
        private ServiceMethodProxy serviceMethodProxy;

        /**
         * 全局拦截器，用来替换{@code @CatServer}上配置的默认拦截器
         * */
        private CatInterceptor globalInterceptor;

        /**
         * {@code @CatServer}配置的拦截器
         * */
        private List<CatServerInterceptor> interceptors;

        /**
         * 拦截器组。
         * 运行时可动态添加移除的拦截器
         * */
        private List<CatInterceptorGroup> interceptorGroups;

        /**
         * 精简模式下参数预处理器
         * */
        private CatParameterResolver parameterResolver;



        public void serverInfo(CatServerInfo serverInfo) {
            boolean noOp = true;
            Class<? extends CatServerInterceptor>[] interceptors = serverInfo.getInterceptors();
            List<CatServerInterceptor> handers = new ArrayList<>(interceptors.length);
            for ( Class<? extends CatServerInterceptor> clazz : interceptors ) {
                if (CatServerInterceptor.class.equals(clazz) ) {
                    // 默认拦截器，只能存在一个。在运行时，会被拦截器组替换
                    if ( noOp ){
                        handers.add(CatServerDefaults.DEFAULT_INTERCEPTOR);
                        noOp = false;
                    }
                } else if ( CatInterceptor.Off.class.equals(clazz) ) {
                    // 关闭所有拦截器
                    handers.add(CatServerDefaults.OFF_INTERCEPTOR);
                } else {
                    // CatServer上自定义拦截器
                    handers.add(CatServerUtil.getBean(clazz));
                }
            }
            this.interceptors = handers;

            CatServerConfiguration serverConfig = serverInfo.getServerConfig();
            CatInterceptor globalInterceptor = serverConfig.getGlobalInterceptor();
            this.globalInterceptor = globalInterceptor != null ? globalInterceptor : serverConfig.getGlobalInterceptor();
            this.interceptorGroups = serverConfig.getInterceptorGroup();
        }


        public Builder interMethod(StandardMethodMetadata interMethod) {
            this.interMethod = interMethod;
            return this;
        }

        public Builder serverMethod(Method serverMethod) {
            this.serverMethod = serverMethod;
            return this;
        }

        public Builder serviceMethodProxy(FastClass fastClass, Class serverBeanClass) {
            Method method = interMethod.getIntrospectedMethod();
            if( fastClass == null ){

                // server对象，被cglib代理
                MethodProxy proxy = MethodProxy.find(serverBeanClass, new Signature(method.getName(), Type.getMethodDescriptor(method)));
                this.serviceMethodProxy = new CglibServiceMethodProxy(proxy);
            } else {

                // server对象，没有、或者不是cglib代理，使用快速处理类
                FastMethod fastMethod = fastClass.getMethod(method);
                this.serviceMethodProxy = new OtherServiceMethodProxy(fastMethod);
            }
            return this;
        }


        public Builder parameterResolver(CatParameterResolver parameterResolver) {
            this.parameterResolver = parameterResolver;
            return this;
        }


        public CatMethodInfo build(){
            return new CatMethodInfo(this);
        }

    }




    /**
     * 在controller的拦截器中，需要执行CatServer实现类方法。
     *
     * 如果Service实现类本身为cglib代理类，则直接执行cglib代理方法。
     *
     * 如果不是，使用FastClass方式调用，避免使用反射。
     *
     * 并且，在环绕在Service实现类方法上的切面，也可以正常执行。
     * */
    protected static abstract class ServiceMethodProxy {
        protected abstract Object invokeProxy(Object target, Object[] args) throws Exception;
    }

    /**
     * cglib代理
     * */
    private static class CglibServiceMethodProxy extends ServiceMethodProxy {
        private final MethodProxy proxy;
        public CglibServiceMethodProxy(MethodProxy proxy) {
            this.proxy = proxy;
        }
        @Override
        protected Object invokeProxy(Object target, Object[] args) throws Exception {
            try {
                return proxy.invoke(target, args);
            } catch ( Throwable throwable ) {
                throw new Exception(throwable);
            }
        }
    }

    /**
     * 使用fastMethod代理
     * */
    private static class OtherServiceMethodProxy extends ServiceMethodProxy {
        private final FastMethod proxy;
        public OtherServiceMethodProxy(FastMethod proxy) {
            this.proxy = proxy;
        }
        @Override
        protected Object invokeProxy(Object target, Object[] args) throws Exception {
            return proxy.invoke(target, args);
        }
    }



}
