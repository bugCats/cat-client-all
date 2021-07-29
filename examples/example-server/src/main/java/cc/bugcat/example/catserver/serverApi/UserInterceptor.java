package cc.bugcat.example.catserver.serverApi;


import com.alibaba.fastjson.JSONObject;
import cc.bugcat.catclient.annotation.CatMethod;
import cc.bugcat.catserver.handler.CatInterceptPoint;
import cc.bugcat.catserver.spi.CatInterceptor;

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
