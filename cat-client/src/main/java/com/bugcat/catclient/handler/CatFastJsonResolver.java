package com.bugcat.catclient.handler;

import com.alibaba.fastjson.JSONObject;
import com.bugcat.catclient.spi.CatJsonResolver;
import com.bugcat.catface.spi.AbstractResponesWrapper;
import com.bugcat.catface.spi.CatTypeReference;

import java.lang.reflect.Type;


public class CatFastJsonResolver implements CatJsonResolver{

    
    @Override
    public <T> T toJavaBean(String text, Type type) {
        return JSONObject.parseObject(text, type);
    }

    
    @Override
    public <T> T toJavaBean(String text, AbstractResponesWrapper<T> wrapper, Type type) {
        CatTypeReference typeRef = wrapper.getWrapperType(type);
        return JSONObject.parseObject(text, typeRef.getType());
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
