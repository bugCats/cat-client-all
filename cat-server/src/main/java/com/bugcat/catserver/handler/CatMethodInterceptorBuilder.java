package com.bugcat.catserver.handler;

import com.bugcat.catserver.beanInfos.CatServerInfo;
import com.bugcat.catserver.utils.CatServerUtil;
import org.springframework.asm.Type;
import org.springframework.cglib.core.Signature;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

public class CatMethodInterceptorBuilder {

    private CatServerInfo serverInfo;
    
    private Class serverClass;
    
    private Object serverBean;
    private Class serverBeanClass;
    
    private FastClass fastClass;
    
    
    
    private StandardMethodMetadata interMethodMetadata;
    private Method realMethod;
    
    private Method cglibInterMethod;
    
    private ServiceProxy serviceProxy;

    
    private CatMethodInterceptorBuilder(){
        
    }
    
    public static CatMethodInterceptorBuilder builder(){
        return new CatMethodInterceptorBuilder();
    }

    public CatMethodInterceptorBuilder serverInfo(CatServerInfo serverInfo){
        this.serverInfo = serverInfo;
        return this;
    }
    public CatMethodInterceptorBuilder serverClass(Class serverClass){
        this.serverClass = serverClass;
        this.serverBean = CatServerUtil.getBean(serverClass);
        this.serverBeanClass = serverBean.getClass();
        if( !ClassUtils.isCglibProxy(serverBeanClass) ){
            this.fastClass = FastClass.create(serverBeanClass);
        }
        return this;
    }
    
    
    public CatMethodInterceptorBuilder interMethodMetadata(StandardMethodMetadata interMethod){
        this.interMethodMetadata = interMethod;
        this.realMethod = interMethod.getIntrospectedMethod();
        return this;
    }
    public CatMethodInterceptorBuilder cglibInterMethod(Method cglibInterMethod){
        this.cglibInterMethod = cglibInterMethod;
        return this;
    }


    public CatMethodInterceptor build(){
        if( fastClass == null ){
            MethodProxy proxy = MethodProxy.find(serverBeanClass, new Signature(realMethod.getName(), Type.getMethodDescriptor(realMethod)));
            this.serviceProxy = new CglibServiceProxy(proxy);
        } else {
            FastMethod fastMethod = fastClass.getMethod(realMethod);
            this.serviceProxy = new OtherServiceProxy(fastMethod);
        }
        return new CatMethodInterceptor(this);
    }
    
    
    /**
     * 在ctrl的拦截器中，需要执行Service实现类方法
     * 如果Service实现类本身为cglib代理类，则直接执行cglib增强后返回
     * 如果不是，使用FastClass方式调用
     * 避免使用反射
     * */
    protected static abstract class ServiceProxy {
        protected abstract Object invoke(Object target, Object[] args) throws Throwable;
    }

    
    private static class CglibServiceProxy extends ServiceProxy {
        private final MethodProxy proxy;
        public CglibServiceProxy(MethodProxy proxy) {
            this.proxy = proxy;
        }

        @Override
        protected Object invoke(Object target, Object[] args) throws Throwable {
            return proxy.invoke(target, args);
        }
    }
    
    
    private static class OtherServiceProxy extends ServiceProxy {
        private final FastMethod proxy;
        public OtherServiceProxy(FastMethod proxy) {
            this.proxy = proxy;
        }

        @Override
        protected Object invoke(Object target, Object[] args) throws Throwable {
            return proxy.invoke(target, args);
        }
    }
    
    
    
    
    public CatServerInfo getServerInfo() {
        return serverInfo;
    }
    public Object getServerBean() {
        return serverBean;
    }
    public StandardMethodMetadata getInterMethodMetadata() {
        return interMethodMetadata;
    }
    public Method getRealMethod() {
        return realMethod;
    }
    public Method getCglibInterMethod() {
        return cglibInterMethod;
    }
    public ServiceProxy getServiceProxy() {
        return serviceProxy;
    }
}
