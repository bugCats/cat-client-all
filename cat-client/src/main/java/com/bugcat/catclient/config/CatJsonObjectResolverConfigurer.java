package com.bugcat.catclient.config;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.bugcat.catface.spi.ResponesWrapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.lang.reflect.Type;

/**
 * http请求时，对象序列化，以及响应反序列化
 * */
@Component
public class CatJsonObjectResolverConfigurer implements InitializingBean{

    
    @Autowired(required = false)
    private JsonObjectResolver resolver;


    @Override
    public void afterPropertiesSet() {
        if ( resolver == null ) {
            resolver = new FastJsonResolver();
        }
    }

    @PreDestroy
    public final JsonObjectResolver getResolver() {
        return resolver;
    }


    
    

    public static interface JsonObjectResolver {
        
        <T> T toJavaBean(String text, Type type);
        
        <T> T toJavaBean(String text, ResponesWrapper<T> wrapper, Type type);
        
        String toJsonString(Object object);
        
        String toXmlString(Object object);
        
    }
    
    
    
    /**
     * fastjson
     * */
    private static class FastJsonResolver implements JsonObjectResolver {
        
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
    
}
