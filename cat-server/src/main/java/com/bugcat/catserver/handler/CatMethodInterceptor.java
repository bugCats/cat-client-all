package com.bugcat.catserver.handler;

import com.bugcat.catface.spi.ResponesWrapper;
import com.bugcat.catserver.beanInfos.CatServerInfo;
import com.bugcat.catserver.spi.CatInterceptor;
import com.bugcat.catserver.utils.CatServerUtil;
import org.springframework.asm.Type;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cglib.core.Signature;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * 通过cglib生成代理类
 * 单例
 * @author bugcat
 * */
public class CatMethodInterceptor implements MethodInterceptor, InitializingBean {
    
    
    private CatServerInfo catServerInfo;
    private StandardMethodMetadata interMethod;         //interface上对于的方法
    private Class serverClass;
    private Method realMethod;

    private MethodProxy realMethodProxy;
    private List<CatInterceptor> handers;
    
    private Function<Object, Object> successToEntry;
    private Function<Object, Object> errorToEntry;
    
    
    public CatMethodInterceptor(CatServerInfo catServerInfo){
        this.catServerInfo = catServerInfo;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        
        Signature signature = new Signature(realMethod.getName(), Type.getReturnType(realMethod), Type.getArgumentTypes(realMethod));
        realMethodProxy = MethodProxy.find(serverClass, signature);
        
        if( handers == null ){
            Class<? extends CatInterceptor>[] handerList = catServerInfo.getHanders();
            handers = new ArrayList<>(handerList.length);
            for(Class<? extends CatInterceptor> clazz : handerList) {
                if( CatInterceptor.class.equals(clazz) ){
                    handers.add(CatInterceptor.instance);
                } else {
                    handers.add(CatServerUtil.getBeanOfType(clazz));
                }
            }
            handers.sort(Comparator.comparingInt(CatInterceptor::getOrder));
        }
        
        ResponesWrapper wrapper = ResponesWrapper.getResponesWrapper(catServerInfo.getWrapper());
        if( wrapper != null ){
            Class wrap = wrapper.getWrapperClass();
            Class<?> returnType = realMethod.getReturnType();
            if( wrap.equals(returnType) || wrap.isAssignableFrom(returnType.getClass()) ){
                successToEntry = value -> value;
                errorToEntry = value -> value;
            } else {
                successToEntry = value -> wrapper.createEntry(value);
                errorToEntry = value -> wrapper.createEntry(value);
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
            point.result = errorToEntry.apply(point.result);
            
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


    public void setServerClass(Class serverClass) {
        this.serverClass = serverClass;
    }

    public void setRealMethod(Method realMethod) {
        this.realMethod = realMethod;
    }

    public void setInterMethods(StandardMethodMetadata interMethod) {
        this.interMethod = interMethod;
    }
}
