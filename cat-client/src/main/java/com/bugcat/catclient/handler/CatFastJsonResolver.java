package com.bugcat.catclient.handler;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.bugcat.catclient.spi.CatJsonResolver;
import com.bugcat.catface.spi.ResponesWrapper;

import java.lang.reflect.Type;


public class CatFastJsonResolver implements CatJsonResolver{

    
    @Override
    public <T> T toJavaBean(String text, Type type) {
        return JSONObject.parseObject(text, type);
    }

    
    @Override
    public <T> T toJavaBean(String text, ResponesWrapper<T> wrapper, Type type) {
        TypeReference<T> typeRef = (TypeReference<T>) wrapper.getWrapperType(type);
        return JSONObject.parseObject(text, typeRef);
    }

    
    @Override
    public String toJsonString(Object object) {
        return JSONObject.toJSONString(object);
    }

    
    @Override
    public String toXmlString(Object object) {
        return null;
    }


}
