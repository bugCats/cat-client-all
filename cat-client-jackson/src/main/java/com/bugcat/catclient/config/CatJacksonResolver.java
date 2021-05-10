package com.bugcat.catclient.config;

import com.bugcat.catclient.spi.CatJsonResolver;
import com.bugcat.catclient.utils.CatClientUtil;
import com.bugcat.catface.spi.AbstractResponesWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;


public class CatJacksonResolver implements CatJsonResolver{

    
    private ObjectMapper mapper;


    public CatJacksonResolver(){
        this(null);
    }

    public CatJacksonResolver(ObjectMapper objectMapper){
        this.mapper = objectMapper;
        if( mapper == null ){
            mapper = CatClientUtil.getBean(ObjectMapper.class);
        }
        if( mapper == null ){
            mapper = new ObjectMapper();
        }        
    }
    
    
    
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
    public <T> T toJavaBean(String jsonString, AbstractResponesWrapper<T> wrapper, Type type) {
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
