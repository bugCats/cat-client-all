package com.bugcat.catserver.handler;

import com.bugcat.catface.spi.ResponesWrapper;
import com.bugcat.catserver.beanInfos.CatServerInfo;
import com.bugcat.catserver.spi.CatInterceptor;
import com.bugcat.catserver.utils.CatServerUtil;
import org.springframework.core.type.StandardMethodMetadata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * 通过cglib生成代理类
 *
 * @author bugcat
 */
public final class CatInterceptorMethodBuilder{

    private StandardMethodMetadata interMethod;         //interface上的方法
    private List<CatInterceptor> handers;
    private ResponesWrapper wrapper;
    
    
    private CatInterceptorMethodBuilder(){}

    public static CatInterceptorMethodBuilder builder() {
        return new CatInterceptorMethodBuilder();
    }
    
    
    public CatInterceptorMethodBuilder interMethod(StandardMethodMetadata interMethod) {
        this.interMethod = interMethod;
        return this;
    }
    
    
    public CatMethodInterceptor build(CatServerInfo catServerInfo){
        Class<? extends CatInterceptor>[] handerList = catServerInfo.getHanders();
        handers = new ArrayList<>(handerList.length);
        for(Class<? extends CatInterceptor> clazz : handerList) {
            if( CatInterceptor.class.equals(clazz) ){
                handers.add(CatInterceptor.instance);
            } else {
                handers.add(CatServerUtil.getBean(clazz));
            }
        }
        handers.sort(Comparator.comparingInt(CatInterceptor::getOrder));
        wrapper = ResponesWrapper.getResponesWrapper(catServerInfo.getWrapper());
        CatMethodInterceptor interceptor = new CatMethodInterceptor(this);
        return interceptor;
    }

    
    
    public StandardMethodMetadata getInterMethod() {
        return interMethod;
    }
    public List<CatInterceptor> getHanders() {
        return handers;
    }
    public ResponesWrapper getWrapper() {
        return wrapper;
    }


  
}
