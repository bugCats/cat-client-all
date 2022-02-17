package cc.bugcat.catserver.handler;

import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catserver.beanInfos.CatServerInfo;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.spi.CatInterceptor;
import cc.bugcat.catserver.spi.CatServerInterceptor;
import cc.bugcat.catserver.spi.CatInterceptorGroup;
import cc.bugcat.catserver.spi.CatServerResultHandler;
import cc.bugcat.catserver.utils.CatServerUtil;
import jdk.internal.org.objectweb.asm.Type;
import org.springframework.cglib.core.Signature;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * controller动态代理拦截器Builder
 * */
public final class CatMethodAopInterceptorBuilder {

    /**
     * 标记的@CatServer注解信息
     * */
    protected CatServerInfo serverInfo;

    /**
     * 被@CatServer标记的server对象
     * */
    protected Object serverBean;

    /**
     * server对象的class
     * 如果server对象被其他组件动态代理，则为代理后的class
     * */
    protected Class serverBeanClass;

    /**
     * cglib 的动态调用类
     * */
    protected FastClass fastClass;

    /**
     * 原interface的方法
     * */
    protected Method realMethod;

    /**
     * 全局拦截器
     * */
    protected CatInterceptor globalInterceptor;

    /**
     * 拦截器
     * */
    protected List<CatServerInterceptor> interceptors;

    /**
     * 运行时拦截器
     * */
    protected List<CatInterceptorGroup> interceptorGroups;

    /**
     * 原interface的方法元数据
     * */
    protected StandardMethodMetadata interMethodMetadata;

    /**
     * 参数预处理器
     * */
    protected CatArgumentResolver argumentResolver;

    /**
     * controller调用CatServer类方法
     * */
    protected ServiceMethodProxy serviceMethodProxy;

    /**
     * 响应结果处理类
     * */
    protected CatServerResultHandler resultHandler;




    public static CatMethodAopInterceptorBuilder builder(){
        return new CatMethodAopInterceptorBuilder();
    }



    private CatMethodAopInterceptorBuilder(){}


    /**
     * 注解对象
     * */
    public CatMethodAopInterceptorBuilder serverInfo(CatServerInfo serverInfo){
        this.serverInfo = serverInfo;
        return this;
    }

    /**
     * 被@CatServer标记的类
     * */
    public CatMethodAopInterceptorBuilder serverClass(Class serverClass){

        this.serverBean = CatServerUtil.getBean(serverClass);
        this.serverBeanClass = serverBean.getClass();

        if( !ClassUtils.isCglibProxy(serverBeanClass) ){// server对象，没有、或者不是cglib代理，使用快速处理类
            this.fastClass = FastClass.create(serverBeanClass);
        }
        return this;
    }

    /**
     * 原interface方法
     * */
    public CatMethodAopInterceptorBuilder interMethodMetadata(StandardMethodMetadata interMethod){
        this.interMethodMetadata = interMethod;
        this.realMethod = interMethod.getIntrospectedMethod();
        return this;
    }


    /**
     * 参数预处理器
     * */
    public CatMethodAopInterceptorBuilder argumentResolver(CatArgumentResolver resolver){
        this.argumentResolver = resolver;
        return this;
    }

    /**
     * 创建controller方法拦截器
     * */
    public CatMethodAopInterceptor build(){
        if( this.fastClass == null ){

            // server对象，被cglib代理
            MethodProxy proxy = MethodProxy.find(serverBeanClass, new Signature(realMethod.getName(), Type.getMethodDescriptor(realMethod)));
            this.serviceMethodProxy = new CglibServiceMethodProxy(proxy);
        } else {

            // server对象，没有、或者不是cglib代理，使用快速处理类
            FastMethod fastMethod = fastClass.getMethod(realMethod);
            this.serviceMethodProxy = new OtherServiceMethodProxy(fastMethod);
        }

        boolean noOp = true;
        Class<? extends CatServerInterceptor>[] interceptors = serverInfo.getInterceptors();
        List<CatServerInterceptor> handers = new ArrayList<>(interceptors.length);
        for ( Class<? extends CatServerInterceptor> clazz : interceptors ) {
            if (CatServerInterceptor.class.equals(clazz) ) {
                // 默认拦截器，只能存在一个。在运行时，会被拦截器组替换
                if ( noOp ){
                    handers.add(CatServerInterceptor.DEFAULT);
                    noOp = false;
                }
            } else if ( CatServerInterceptor.Off.class.equals(clazz) ) {
                // 关闭所有拦截器
                handers.add(CatServerInterceptor.OFF);
            } else {
                // CatServer上自定义拦截器
                handers.add(CatServerUtil.getBean(clazz));
            }
        }
        this.interceptors = handers;

        AbstractResponesWrapper wrapperHandler = serverInfo.getWrapperHandler();
        if ( wrapperHandler != null ) {
            this.resultHandler = CatServerResultHandler.build(wrapperHandler, realMethod.getReturnType());
        } else {
            this.resultHandler = CatServerResultHandler.build();
        }

        CatServerConfiguration serverConfig = serverInfo.getServerConfig();
        CatInterceptor globalInterceptor = serverConfig.globalInterceptor().get();
        this.globalInterceptor = globalInterceptor != null ? globalInterceptor : CatServerInterceptor.DEFAULT;
        this.interceptorGroups = serverConfig.interceptorGroup().get();

        return new CatMethodAopInterceptor(this);
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
