package cc.bugcat.example.catserver.serverApi;


import cc.bugcat.catserver.handler.CatInterceptPoint;
import cc.bugcat.catserver.handler.CatServerContextHolder;
import cc.bugcat.catserver.spi.CatServerInterceptor;

import java.util.Map;

/**
 * 拦截器
 * */
public class UserInterceptor implements CatServerInterceptor {


    @Override
    public Object postHandle(CatServerContextHolder contextHolder) throws Throwable {
        System.out.println("自定义拦截器：UserInterceptor1 执行前");
        Object result = contextHolder.proceedRequest();
        System.out.println("自定义拦截器：UserInterceptor1 执行后");
        CatInterceptPoint point = contextHolder.getInterceptPoint();
        
        Map<String, Object> noteMap = point.getNoteMap();
        Object uname = noteMap.get("uname");
        if( uname != null ){
            System.out.println(uname);
        }
        
        return result;
    }
}
