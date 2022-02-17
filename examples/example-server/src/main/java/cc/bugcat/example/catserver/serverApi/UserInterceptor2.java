package cc.bugcat.example.catserver.serverApi;


import cc.bugcat.catserver.handler.CatServerContextHolder;
import cc.bugcat.catserver.spi.CatServerInterceptor;

/**
 * 拦截器
 * */
public class UserInterceptor2 implements CatServerInterceptor {


    @Override
    public Object postHandle(CatServerContextHolder contextHolder) throws Exception {
        System.out.println("UserInterceptor2 执行前");
//        String ss = null;
//        ss.contains("122");
        Object result = contextHolder.executeRequest();
        System.out.println("UserInterceptor2 执行后");
        return result;
    }

}