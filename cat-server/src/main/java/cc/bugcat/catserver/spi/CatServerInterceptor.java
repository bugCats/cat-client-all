package cc.bugcat.catserver.spi;


import cc.bugcat.catserver.annotation.CatServer;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.handler.CatInterceptPoint;
import cc.bugcat.catserver.handler.CatMethodAopInterceptor;
import cc.bugcat.catserver.handler.CatServerContextHolder;


/**
 *
 * CatServer类上拦截器。
 *
 * 被{@code CatServer}标记的类，会作为Controller注册到系统中，
 * 如果需要对这些类权限控制，可以通过Aop切面控制，也可以使用自定义拦截器处理；
 * 注意，此拦截器只会通过http访问时生效，如果直接内部调用标记类，是不会生效！
 * 
 * {@link CatServer#interceptors()}中
 *  1、如果需要使用全局默认拦截器，使用{@code CatServerInterceptor.class}占位。
 *      最终会被{@link CatServerConfiguration#getServerInterceptor()}替换。
 *  2、如果不需要自定义拦截器和全局拦截器，可以使用{@code CatServerInterceptor.NoOp.class}；
 *  3、默认都会匹配拦截器组，如果满足条件，则会在所有拦截器之前执行。如果不需要拦截组，使用{@code CatServerInterceptor.GroupOff.class}关闭
 * 
 * 
 *
 * @see CatMethodAopInterceptor
 *
 * @author bugcat
 * */
public interface CatServerInterceptor {

    /**
     * 空的拦截器。
     * 如果某个CatServer不想使用{@link CatServer#interceptors()}拦截器，可以使用{@code CatServerInterceptor.NoOp.class}关闭。
     * 注意，仍然会执行拦截组！
     * */
    public static final class NoOp implements CatServerInterceptor {

    }
    
    /**
     * 关闭拦截器组。
     * */
    public static final class GroupOff implements CatServerInterceptor {

    }
    
    

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

}
