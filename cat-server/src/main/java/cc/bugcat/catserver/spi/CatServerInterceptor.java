package cc.bugcat.catserver.spi;


import cc.bugcat.catserver.handler.CatInterceptPoint;
import cc.bugcat.catserver.handler.CatMethodAopInterceptor;
import cc.bugcat.catserver.handler.CatServerContextHolder;


/**
 *
 * CatServer类上拦截器
 *
 * 被{@code CatServer}标记的类，会作为Controller注册到系统中，
 * 如果需要对这些类权限控制，可以通过Aop切面控制，也可以使用自定义拦截器处理
 *
 * 注意，此拦截器只会通过http访问时生效，如果直接内部调用标记类，是不会生效！
 *
 * 默认会被配置文件中匹配的拦截器组替换
 *
 * @see CatMethodAopInterceptor
 *
 * @author bugcat
 * */
public interface CatServerInterceptor {


    /**
     * 是否需要执行拦截器。
     *
     * 某个拦截器，可能被多个{@code @CatServer}使用，
     * 但是该拦截器内容可能是按需执行，如果返回false，则不会加入到拦截器链中。
     *
     * 可以在此处做访问权限校验，
     * 如果抛出异常，后续的postHandle不会执行。
     * */
    default boolean preHandle(CatInterceptPoint point) throws Exception {
        return true;
    }


    /**
     * 执行拦截器
     * */
    default Object postHandle(CatServerContextHolder contextHolder) throws Throwable {
        return contextHolder.proceedRequest();
    }


    /**
     * 运行时拦截器组，默认情况在所有自定义拦截器之前执行。
     * 支持可以手动调整顺序
     * */
    public static final class Group implements CatServerInterceptor {

    }
    
    /**
     * 关闭所有拦截器，包括运行时匹配情况
     * */
    public static final class Off implements CatServerInterceptor {

    }


}
