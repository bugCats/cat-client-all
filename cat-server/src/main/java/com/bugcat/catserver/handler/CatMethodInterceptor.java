package com.bugcat.catserver.handler;

import com.bugcat.catface.spi.AbstractResponesWrapper;
import com.bugcat.catserver.beanInfos.CatServerInfo;
import com.bugcat.catserver.spi.CatInterceptor;
import com.bugcat.catserver.utils.CatServerUtil;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * 通过cglib生成代理类
 * 单例
 * @author bugcat
 * */
public final class CatMethodInterceptor implements MethodInterceptor{
    
   
    private final StandardMethodMetadata interMethod;         //interface上对应的真实方法
    private final Method realMethod;                          //interface上对应的真实方法

    private final List<CatInterceptor> handers;
    
    private final CatArgumentResolver argumentResolver;    //参数预处理器
    
    private final Function<Object, Object> successToEntry;
    private final Function<Throwable, Object> errorToEntry;


    public CatMethodInterceptor(StandardMethodMetadata interMethod, Method catInterMethod, CatServerInfo serverInfo) {
        this.argumentResolver = CatArgumentResolver.build(serverInfo, catInterMethod);
        
        Class<? extends CatInterceptor>[] handerList = serverInfo.getHanders();
        List<CatInterceptor> handers = new ArrayList<>(handerList.length);
        for(Class<? extends CatInterceptor> clazz : handerList) {
            if( CatInterceptor.class.equals(clazz) ){
                handers.add(CatInterceptor.defaults);
            } else {
                handers.add(CatServerUtil.getBean(clazz));
            }
        }
        handers.sort(Comparator.comparingInt(CatInterceptor::getOrder));

        this.interMethod = interMethod;
        this.realMethod = interMethod.getIntrospectedMethod();
        this.handers = handers;
        
        Class wrap = serverInfo.getWarpClass();
        if( wrap != null ){
            Class<?> returnType = realMethod.getReturnType();
            if( wrap.equals(returnType) || wrap.isAssignableFrom(returnType.getClass()) ){
                successToEntry = value -> value;
                errorToEntry = value -> value;
            } else {
                AbstractResponesWrapper wrapper = serverInfo.getWarp();
                successToEntry = value -> wrapper.createEntryOnSuccess(value, realMethod.getGenericReturnType());
                errorToEntry = value -> wrapper.createEntryOnException(value, realMethod.getGenericReturnType());
            }
        } else {
            successToEntry = value -> value;
            errorToEntry = value -> value;
        }
    }
    
    @Override
    public Object intercept (Object ctrl, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        HttpServletResponse response = attr.getResponse();

        args = argumentResolver.resolveNameArgument(request, args);
        
        Class serverClass = CatServiceCtrlInterceptor.getServerClass(ctrl);
        Object server = CatServerUtil.getBean(serverClass);
        
        CatInterceptPoint point = new CatInterceptPoint(request, response, server, interMethod, args);

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

            point.result = successToEntry.apply(realMethod.invoke(server, args));

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
