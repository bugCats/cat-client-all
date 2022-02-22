package cc.bugcat.example.catserver.serverApi;


import cc.bugcat.catserver.handler.CatServerContextHolder;
import cc.bugcat.catserver.spi.CatServerInterceptor;

/**
 * 拦截器
 * */
public class UserInterceptor implements CatServerInterceptor {


    @Override
    public Object postHandle(CatServerContextHolder contextHolder) throws Throwable {
        System.out.println("UserInterceptor 执行前");
        Object result = contextHolder.executeRequest();
        System.out.println("UserInterceptor 执行后");
        return result;
    }
}
