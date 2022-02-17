package cc.bugcat.catserver.spi;


import cc.bugcat.catserver.handler.CatMethodAopInterceptor;
import cc.bugcat.catserver.handler.CatInterceptPoint;
import cc.bugcat.catserver.handler.CatServerContextHolder;


/**
 *
 * 拦截器
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
 * */
public interface CatServerInterceptor extends CatInterceptor {


    /**
     * 关闭拦截器
     * */
    public static class Off implements CatServerInterceptor {

    }

    /**
     * 默认拦截器组
     * */
    public final static CatServerInterceptor DEFAULT = new CatServerInterceptor() {};
    /**
     * 关闭拦截器
     * */
    public final static CatServerInterceptor OFF = new CatServerInterceptor.Off() {};




    /**
     * 是否需要执行拦截器
     *
     * 某个拦截器，可能被多个{@code @CatServer}使用，
     * 但是该拦截器内容可能是按需执行，如果返回false，则不会加入到拦截器链中
     *
     * 可以在此处做访问权限校验
     *
     * 如果抛出异常，后续的postHandle不会执行。
     * */
    default boolean preHandle(CatInterceptPoint point) throws Exception {
        return true;
    }


    /**
     * 执行拦截器
     * */
    @Override
    default Object postHandle(CatServerContextHolder contextHolder) throws Exception {
        return contextHolder.executeRequest();
    }


}
