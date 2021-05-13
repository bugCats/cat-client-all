package com.bugcat.example.catserver.serverApi;


import com.alibaba.fastjson.JSONObject;
import com.bugcat.catclient.annotation.CatMethod;
import com.bugcat.catserver.handler.CatInterceptPoint;
import com.bugcat.catserver.spi.CatInterceptor;

import java.util.Map;

/**
 * 拦截器
 * */
public class UserInterceptor extends CatInterceptor{


    

    @Override
    public void befor(CatInterceptPoint point) {
        Map<String, Object> annotations = point.getAnnotations(CatMethod.class);
        System.out.println("UserInterceptor.befor" + JSONObject.toJSONString(point.getArgs()));
    }

    @Override
    public void after(CatInterceptPoint point) {
        System.out.println("UserInterceptor.after" + JSONObject.toJSONString(point.getResult()));
    }

    

}
