package com.bugcat.catserver.beanInfos;

import com.bugcat.catface.spi.ResponesWrapper;
import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.catserver.spi.CatInterceptor;
import org.springframework.core.annotation.AnnotationAttributes;

public class CatServerInfo {

    private String beanName;

    private Class<? extends ResponesWrapper> wrapper;      //响应包裹类
    
    private Class<? extends CatInterceptor>[] handers;

    
    public CatServerInfo(AnnotationAttributes attr) {
        String beanName = attr.getString("value");
        this.beanName = CatToosUtil.defaultIfBlank(beanName, CatToosUtil.uncapitalize(attr.getString("beanName")));

        //响应包裹类，如果是ResponesWrapper.default，代表没有设置
        Class<? extends ResponesWrapper> wrapper = attr.getClass("wrapper");
        this.wrapper = wrapper == null || ResponesWrapper.Default.class.equals(wrapper) ? null : wrapper;
        
        this.handers = (Class<? extends CatInterceptor>[]) attr.getClassArray("handers");
    }


    public String getBeanName() {
        return beanName;
    }
    public Class<? extends ResponesWrapper> getWrapper() {
        return wrapper;
    }
    public Class<? extends CatInterceptor>[] getHanders() {
        return handers;
    }
    
}
