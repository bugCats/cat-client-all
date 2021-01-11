package com.bugcat.catserver.beanInfos;

import com.bugcat.catface.annotation.CatResponesWrapper;
import com.bugcat.catface.spi.ResponesWrapper;
import com.bugcat.catserver.annotation.CatServer;
import com.bugcat.catserver.spi.CatInterceptor;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.StandardAnnotationMetadata;

import java.util.Map;

public class CatServerInfo {

    private Class<? extends ResponesWrapper> wrapper;      //响应包装器类
    
    private Class<? extends CatInterceptor>[] handers;

    private CatServerInfo(AnnotationAttributes attr) {
        
        //响应包装器类，如果是ResponesWrapper.default，代表没有设置
        Class<? extends ResponesWrapper> wrapper = attr.getClass("wrapper");
        this.wrapper = wrapper == null || ResponesWrapper.Default.class.equals(wrapper) ? null : wrapper;
        
        this.handers = (Class<? extends CatInterceptor>[]) attr.getClassArray("handers");
    }


    

    public final static CatServerInfo buildServerInfo(Class inter) {
        AnnotationAttributes attributes = CatServerInfo.getAttributes(inter);
        CatServerInfo serverInfo = new CatServerInfo(attributes);
        return serverInfo;
    }
    
    private static AnnotationAttributes getAttributes(Class inter) {
        StandardAnnotationMetadata metadata = new StandardAnnotationMetadata(inter);
        AnnotationAttributes client = new AnnotationAttributes(metadata.getAnnotationAttributes(CatServer.class.getName()));
        Map<String, Object> wrapper = responesWrap(inter);
        if( wrapper != null ){
            client.put("wrapper", wrapper.get("value"));
        } else {
            client.put("wrapper", ResponesWrapper.Default.class);
        }
        return client;
    }

    private static Map<String, Object> responesWrap(Class inter){
        StandardAnnotationMetadata metadata = new StandardAnnotationMetadata(inter);
        Map<String, Object> wrapper = metadata.getAnnotationAttributes(CatResponesWrapper.class.getName());
        if( wrapper == null ){
            for ( Class clazz : inter.getInterfaces() ) {
                wrapper = responesWrap(clazz);
                if( wrapper != null ){
                    return wrapper;
                }
            }
        }
        return wrapper;
    }
    

    public Class<? extends ResponesWrapper> getWrapper() {
        return wrapper;
    }
    public Class<? extends CatInterceptor>[] getHanders() {
        return handers;
    }
    
}
