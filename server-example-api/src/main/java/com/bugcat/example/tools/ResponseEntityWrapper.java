package com.bugcat.example.tools;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.bugcat.catface.spi.ResponesWrapper;

import java.lang.reflect.Type;

/**
 * http响应包装器类处理
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
    public <T> Object getWrapperType(Type type){
        return new TypeReference<ResponseEntity<T>>(type){};
    }

    /**
     * 校验业务
     * 直接抛出异常
     */
    @Override
    public void checkValid(ResponseEntity obj) {
        if( ResponseEntity.succ.equals(obj.getErrCode())){
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


    @Override
    public ResponseEntity createEntryOnSuccess(Object value, Type returnType) {
        return ResponseEntity.ok(value);
    }

    @Override
    public ResponseEntity createEntryOnException(Throwable ex, Type returnType) {
        String err = "{\"err\":\"-1\"}";
        Object data = JSONObject.parseObject(err, returnType);
        return ResponseEntity.fail("-1", ex.getMessage());
    }
}
