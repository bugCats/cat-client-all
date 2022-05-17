package cc.bugcat.catserver.annotation;


import cc.bugcat.catface.annotation.CatNote;
import cc.bugcat.catserver.spi.CatServerInterceptor;
import cc.bugcat.catserver.spi.CatResultHandler;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.*;



/**
 * 标记在类上，使其暴露成API接口。
 *
 * 该类必须实现类似于 feign-interface 的接口，
 *
 * 也可以采用 cat-client。
 *
 *
 * @author bugcat
 * */
@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Component
@ResponseBody
public @interface CatServer {


    /**
     * 组件别名
     * */
    @AliasFor(value = "value", annotation = Component.class)
    String value() default "";


    /**
     * 分组标记，
     * {@code @CatNote(key="name", value="bugcat")}：直接字符串；
     * {@code @CatNote(key="host", value="${host}")}：从环境变量中获取；
     * {@code @CatNote("bugcat")}：省略key，最终key与value值相同；
     * */
    CatNote[] tags() default {};


    /**
     * 通过http调用时，CatServer类上的拦截器链。
     * CatServerInterceptor.class 表示启用全局的默认拦截器配置，最终该位置会被全局配置的拦截器替换；
     * CatServerInterceptor.Group.class 表示运行时匹配的拦截器，最终该位置会被拦截器组替换。如果没有匹配上，则什么都不执行；
     * CatServerInterceptor.Off.class 表示关闭所有拦截器。
     *
     * {@code @CatServer}：启用默认拦截器，默认拦截器在CatServerConfiguration#getGlobalInterceptor()指定；
     * {@code @CatServer(interceptors = CatServerInterceptor.class)}：启用默认拦截器；
     * {@code @CatServer(interceptors = {CatServerInterceptor.class, UserInterceptor.class})}：启用默认拦截器和自定义拦截器
     * {@code @CatServer(interceptors = UserInterceptor.class)}：仅启用自定义拦截器
     * */
    Class<? extends CatServerInterceptor>[] interceptors() default CatServerInterceptor.class;

    /**
     * 异常处理类
     * */
    Class<? extends CatResultHandler> resultHandler() default CatResultHandler.class;
    
}
