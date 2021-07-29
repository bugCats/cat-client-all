package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.beanInfos.CatMethodInfo;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;


/**
 * 动态代理生成的类{@link CatClient}
 * */
public interface CatMethodInterceptor {

    
    Object intercept (CatClientInfo clientInfo,
                      CatMethodInfo methodInfo,
                      Object target,
                      Method method, 
                      Object[] args, 
                      MethodProxy methodProxy) throws Throwable;
    
}
