package cc.bugcat.catserver.spi;

import cc.bugcat.catserver.handler.CatServerContextHolder;


/**
 * 全局controller拦截器
 * 一般用于记录日志
 * */
public interface CatInterceptor {


    /**
     * 执行拦截器
     * */
    default Object postHandle(CatServerContextHolder contextHolder) throws Exception {
        return contextHolder.executeRequest();
    }

}
