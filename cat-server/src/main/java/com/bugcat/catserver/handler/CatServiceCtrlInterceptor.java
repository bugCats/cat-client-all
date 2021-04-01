package com.bugcat.catserver.handler;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import java.lang.reflect.Method;

public final class CatServiceCtrlInterceptor implements MethodInterceptor{
    
    private final static MethodInterceptor defaults = new MethodInterceptor() {
        @Override
        public Object intercept (Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            return methodProxy.invokeSuper(target, args);
        }
    };


    private Class serverClass;
    private CatServiceCtrlInterceptor(){}
    
    
    public static MethodInterceptor getInstance() {
        return new CatServiceCtrlInterceptor();
    }
    public static MethodInterceptor getDefault() {
        return defaults;
    }
    
    public static Class getServerClass(Object ctrl) {
        return ((CatServiceCtrl) ctrl).$bugcat$serverInfo(null);
    }
    public static void setServerClass(Object ctrl, Class serverClass) {
        ((CatServiceCtrl) ctrl).$bugcat$serverInfo(serverClass);
    }
    public static Class getServerClass() {
        return CatServiceCtrl.class;
    }
    
    
    
    @Override
    public Object intercept(Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if( args.length > 0 && args[0] != null ){
            serverClass = (Class) args[0];
        }
        return serverClass;
    }


    public static interface CatServiceCtrl{
        Class $bugcat$serverInfo(Class serverClass);
    }

    
}
