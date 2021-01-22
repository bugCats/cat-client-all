package com.bugcat.catclient.config;

import com.bugcat.catface.spi.ResponesWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class CatJacksonResolver implements CatJsonObjectResolverConfigurer.JsonObjectResolver {

    
    private final ObjectMapper mapper = new ObjectMapper();

    
    @Override
    public <T> T toJavaBean(String jsonString, Type type) {
        try {
            JavaType javaType = mapper.getTypeFactory().constructType(type);
            return mapper.readValue(jsonString, javaType);
        } catch ( Exception ex ) {
            throw new RuntimeException("对象反序列化异常", ex);
        }
    }

    
    @Override
    public <T> T toJavaBean(String jsonString, ResponesWrapper<T> wrapper, Type type) {
        TypeReference<T> typeRef = (TypeReference<T>) wrapper.getWrapperType(type);
        try {
            return mapper.readValue(jsonString, typeRef);
        } catch ( Exception ex ) {
            throw new RuntimeException("对象反序列化异常", ex);
        }
    }

    
    @Override
    public String toJsonString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch ( Exception ex ) {
            throw new RuntimeException("对象序列化异常", ex);
        }
    }

    @Override
    public String toXmlString(Object object) {
        return null;
    }
}
