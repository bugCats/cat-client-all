package com.bugcat.catface.handler;

import com.alibaba.fastjson.JSONObject;


/**
 * 精简模式参数序列化
 * */
public class CatTransport implements Stringable {

    
    private final Object value;

    public CatTransport(Object value) {
        this.value = value;
    }


    @Override
    public String serialize() {
        return JSONObject.toJSONString(value);
    }

}
