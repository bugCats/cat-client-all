package cc.bugcat.catserver.spi;

import cc.bugcat.catserver.handler.CatServerContextHolder;


/**
 * 全局controller拦截器
 * 一般用于记录日志
 *
 *
 * @author bugcat
 * */
public interface CatInterceptor {


    /**
     * 执行拦截器
     * */
    default Object postHandle(CatServerContextHolder contextHolder) throws Throwable {
        return contextHolder.executeRequest();
    }



    /**
     * 关闭拦截器
     * */
    public static final class Off implements CatServerInterceptor {

    }

}
