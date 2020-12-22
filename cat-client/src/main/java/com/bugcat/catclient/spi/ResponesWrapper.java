package com.bugcat.catclient.spi;

import com.alibaba.fastjson.TypeReference;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * http响应包裹类处理
 *
 * <pre>
 *     
 *  比喻远程服务端的响应，统一使用了ResponseEntity实体进行封装，具体的业务对象，是ResponseEntity通过泛型确定的一个属性
 *  那么可以使用包裹类处理，去掉ResponseEntity，interface中方法直接返回业务对象
 *  
 * </pre>
 * 
 * @author bugcat
 * */
public abstract class ResponesWrapper<T> {


    /**
     * 响应包裹类map
     * */
    protected final static Map<Class, ResponesWrapper> wrapperMap = new HashMap<>();

    
    public final static ResponesWrapper getResponesWrapper(Class<? extends ResponesWrapper> clazz){
        if( clazz == null ){
            return null;
        }
        ResponesWrapper wrapper = wrapperMap.get(clazz);
        if( wrapper == null ){
            synchronized ( wrapperMap ) {
                wrapper = wrapperMap.get(clazz);
                if ( wrapper == null ) {
                    try {
                        wrapper = clazz.newInstance();
                        wrapperMap.put(clazz, wrapper);
                    } catch ( Exception e ) {
                        throw new RuntimeException("获取" + clazz.getSimpleName() + "包裹类异常");
                    }
                }
            }
        }
        return wrapper;
    }
    
    
    /**
     * 获取包裹类class
     * */
    public abstract Class<T> getWrapperClass();

    /**
     * 获取json转对象泛型
     * @param type 业务类型的Type
     * */
    public abstract <M> TypeReference getWrapperType(Type type);
    
    /**
     * 校验业务
     * 直接抛出异常
     * */
    public abstract void checkValid(T obj) throws Exception;

    /**
     * 得到业务数据
     * */
    public abstract Object getValue(T obj);
    
    
    
    
    /**
     * 默认
     * */
    public final static class Default extends ResponesWrapper<Object>{
        @Override
        public Class<Object> getWrapperClass() {
            return null;
        }
        @Override
        public <M> TypeReference getWrapperType(Type type) {
            return null;
        }
        @Override
        public void checkValid(Object obj) throws Exception {}
        @Override
        public Object getValue(Object obj) {
            return null;
        }
    }
    
    
    
    /**
     * 举个栗子
     * */
    private final static class ResponseEntityWrapper extends ResponesWrapper<ResponseEntity>{
        @Override
        public Class<ResponseEntity> getWrapperClass() {
            return ResponseEntity.class;
        }

        @Override
        public <M> TypeReference getWrapperType(Type type) {
            return new TypeReference<ResponseEntity<M>>(type){};
        }

        @Override
        public void checkValid(ResponseEntity obj) throws Exception {
            
        }

        @Override
        public Object getValue(ResponseEntity obj) {
            return obj.getBody();
        }
    }
}
