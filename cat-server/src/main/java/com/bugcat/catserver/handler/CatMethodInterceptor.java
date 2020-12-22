package com.bugcat.catserver.handler;

import com.bugcat.catserver.beanInfos.CatServerInfo;
import com.bugcat.catserver.spi.CatInterceptor;
import com.bugcat.catserver.utils.CatToosUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 通过cglib生成代理类
 * 单例
 * @author bugcat
 * */
public class CatMethodInterceptor implements MethodInterceptor, InitializingBean {
    
    private final CatServerInfo catServerInfo;
    private final List<StandardMethodMetadata> interMethods;         //interface上对于的方法
    private List<CatInterceptor> handers;
    
    public CatMethodInterceptor(CatServerInfo catServerInfo, List<StandardMethodMetadata> interMethods){
        this.catServerInfo = catServerInfo;
        this.interMethods = Collections.unmodifiableList(interMethods);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if( handers == null ){
            Class<? extends CatInterceptor>[] handerList = catServerInfo.getHanders();
            handers = new ArrayList<>(handerList.length);
            for(Class<? extends CatInterceptor> clazz : handerList) {
                if( CatInterceptor.Defualt.class.equals(clazz) ){
                    handers.add(CatInterceptor.Defualt.instance);
                } else {
                    handers.add(CatToosUtil.getBeanOfType(clazz));
                }
            }
            handers.sort(Comparator.comparingInt(CatInterceptor::getOrder));
        }
    }

    
    @Override
    public Object intercept (Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        HttpServletResponse response = attr.getResponse();

        CatInterceptPoint point = new CatInterceptPoint(request, response, target, method, interMethods, args);

        List<CatInterceptor> active = new ArrayList<>(handers.size());

        Object respObj = null;

        try {
            
            for( CatInterceptor hander : handers ){
                if( hander.preHandle(point) ){
                    active.add(hander);
                }
            }

            for(CatInterceptor hander : active){
                hander.befor(point);
            }
            
            respObj = methodProxy.invokeSuper(target, args);
            point.result = respObj;
            
        } catch ( Exception ex ) {
            point.exception = ex;
        } finally {
            for(int i = active.size() - 1; i >= 0; i -- ){
                CatInterceptor hander = active.get(i);
                hander.after(point);
            }
        }
        return respObj;
    }


}
