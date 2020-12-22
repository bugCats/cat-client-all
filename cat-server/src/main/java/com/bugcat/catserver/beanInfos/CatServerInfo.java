package com.bugcat.catserver.beanInfos;

import com.bugcat.catserver.spi.CatInterceptor;
import com.bugcat.catserver.utils.CatToosUtil;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.ArrayList;
import java.util.List;

public class CatServerInfo {

    private String beanName;
    
    private Class<? extends CatInterceptor>[] handers;

    
    public CatServerInfo(AnnotationAttributes attr) {
        String beanName = attr.getString("value");
        this.beanName = CatToosUtil.defaultIfBlank(beanName, CatToosUtil.uncapitalize(attr.getString("beanName")));
        this.handers = (Class<? extends CatInterceptor>[]) attr.getClassArray("handers");
    }


    public String getBeanName() {
        return beanName;
    }
    public Class<? extends CatInterceptor>[] getHanders() {
        return handers;
    }
    
}
