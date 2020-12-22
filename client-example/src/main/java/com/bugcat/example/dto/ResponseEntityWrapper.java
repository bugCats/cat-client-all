package com.bugcat.example.dto;

import com.alibaba.fastjson.TypeReference;
import com.bugcat.catface.spi.ResponesWrapper;

import java.lang.reflect.Type;

/**
 * http响应包裹类处理
 * 
 * @see ResponesWrapper
 * @author bugcat
 * */
public class ResponseEntityWrapper extends ResponesWrapper<ResponseEntity> {

    
    @Override
    public Class<ResponseEntity> getWrapperClass() {
        return ResponseEntity.class;
    }

    /**
     * 获取json转对象泛型
     */
    @Override
    public <M> TypeReference getWrapperType(Type type){
        return new TypeReference<ResponseEntity<M>>(type){};
    }

    /**
     * 校验业务
     * 直接抛出异常
     */
    @Override
    public void checkValid(ResponseEntity obj) {
        if(ResponseEntity.succ.equals(obj.getErrCode())){
            //正常
        } else {
            //业务异常，可以直接继续抛出，在公共的异常处理类中，统一处理
            throw new RuntimeException("[" + obj.getErrCode() + "]" + obj.getErrMsg());
        }
    }

    @Override
    public Object getValue(ResponseEntity obj) {
        return obj.getData();
    }
}
