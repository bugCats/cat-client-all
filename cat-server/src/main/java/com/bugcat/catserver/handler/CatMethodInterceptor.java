package com.bugcat.catserver.handler;

import com.bugcat.catface.spi.ResponesWrapper;
import com.bugcat.catserver.handler.CatInterceptorBuilders.MethodBuilder;
import com.bugcat.catserver.spi.CatInterceptor;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 通过cglib生成代理类
 * 单例
 * @author bugcat
 * */
public final class CatMethodInterceptor implements MethodInterceptor {
    
    
    private StandardMethodMetadata interMethod;         //interface上对于的方法
    private Method realMethod;

    private MethodProxy realMethodProxy;
    private List<CatInterceptor> handers;
    
    private Function<Object, Object> successToEntry;
    private Function<Throwable, Object> errorToEntry;

    
    protected void initializing(MethodBuilder builder) {
        
        interMethod = builder.getInterMethod();
        realMethod = builder.getRealMethod();
        
        realMethodProxy = builder.getMethodProxy();
        handers = builder.getHanders();
        
        ResponesWrapper wrapper = builder.getWrapper();
        if( wrapper != null ){
            Class wrap = wrapper.getWrapperClass();
            Class<?> returnType = realMethod.getReturnType();
            if( wrap.equals(returnType) || wrap.isAssignableFrom(returnType.getClass()) ){
                successToEntry = value -> value;
                errorToEntry = value -> value;
            } else {
                successToEntry = value -> wrapper.createEntryOnSuccess(value, realMethod.getGenericReturnType());
                errorToEntry = value -> wrapper.createEntryOnException(value, realMethod.getGenericReturnType());
            }
        } else {
            successToEntry = value -> value;
            errorToEntry = value -> value;
        }
    }

    
    
    @Override
    public Object intercept (Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        HttpServletResponse response = attr.getResponse();

        CatInterceptPoint point = new CatInterceptPoint(request, response, target, realMethod, interMethod, args);

        List<CatInterceptor> active = new ArrayList<>(handers.size());

        for( CatInterceptor hander : handers ){
            if( hander.preHandle(point) ){
                active.add(hander);
            }
        }
        
        Throwable exception = null;

        try {
            
            for(CatInterceptor hander : active){
                hander.befor(point);
            }

            point.result = successToEntry.apply(realMethodProxy.invokeSuper(target, args));

            for(int i = active.size() - 1; i >= 0; i -- ){
                CatInterceptor hander = active.get(i);
                hander.after(point);
            }
            
        } catch ( Throwable ex ) {
            
            exception = ex;
            point.result = errorToEntry.apply(ex);
            
        } finally {
            
            if( exception != null ){
                if( active.size() > 0 ){
                    for(int i = active.size() - 1; i >= 0; i -- ){
                        CatInterceptor hander = active.get(i);
                        hander.exception(point, exception);
                    }
                } else {
                    throw exception;
                }
            }
        }
        
        return point.result;
    }

}
