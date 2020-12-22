package com.bugcat.example.catserver.serverApi;


import com.alibaba.fastjson.JSONObject;
import com.bugcat.catclient.annotation.CatMethod;
import com.bugcat.catserver.handler.CatInterceptPoint;
import com.bugcat.catserver.spi.CatInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Component
public class UserInterceptor implements CatInterceptor{

    @Override
    public boolean preHandle(CatInterceptPoint point) {
        return true;
    }

    @Override
    public void befor(CatInterceptPoint point) throws Exception {
        Map<String, Object> annotations = point.getAnnotations(CatMethod.class);
        System.out.println("UserInterceptor.befor" + JSONObject.toJSONString(point.getArgs()));
    }

    @Override
    public void after(CatInterceptPoint point) throws Exception {
        System.out.println("UserInterceptor.after" + JSONObject.toJSONString(point.getResult()));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
