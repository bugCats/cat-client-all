package com.bugcat.catclient.handler;

import com.bugcat.catclient.beanInfos.CatClientInfo;
import com.bugcat.catclient.beanInfos.CatMethodInfo;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;


public interface CatMethodInterceptor {

    
    Object intercept (CatClientInfo catClientInfo, 
                      CatMethodInfo methodInfo,
                      Object target,
                      Method method, 
                      Object[] args, 
                      MethodProxy methodProxy) throws Throwable;
    
}
