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
    
    
    /**
     * 自定义方法拦截器
     * */
    public static MethodInterceptor create() {
        return new CatServiceCtrlInterceptor();
    }
    
    /**
     * 默认方法拦截器
     * */
    public static MethodInterceptor getDefault() {
        return defaults;
    }
    
    /**
     * 从ctrl对象中取Service class
     * */
    public static Class getServerClass(Object ctrl) {
        return ((CatServiceCtrl) ctrl).$bugcat$serverInfo(null);
    }
    
    /**
     * 将Service class 存入ctrl对象中
     * */
    public static void setServerClass(Object ctrl, Class serverClass) {
        ((CatServiceCtrl) ctrl).$bugcat$serverInfo(serverClass);
    }
    
    
    
    public static Class getCatServiceCtrlClass() {
        return CatServiceCtrl.class;
    }
    
    
    
    /**
     * 通过Service类动态生成ctrl之后，再把Service的class赋值给ctrl属性
     * */
    @Override
    public Object intercept(Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if( args.length > 0 && args[0] != null ){   //当入参个数不等于0，赋值；否则取值
            serverClass = (Class) args[0];
        }
        return serverClass;
    }


    /**
     * 通过Service类动态生成ctrl之后，再把Service的class赋值给ctrl属性
     * */
    public interface CatServiceCtrl {
        Class $bugcat$serverInfo(Class serverClass);
    }

    
}
