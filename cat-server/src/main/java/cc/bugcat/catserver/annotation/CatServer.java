package cc.bugcat.catserver.annotation;


import cc.bugcat.catface.annotation.CatNote;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.spi.CatResultHandler;
import cc.bugcat.catserver.spi.CatServerInterceptor;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



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
     * <pre>
     * {@code @CatNote(key="name", value="bugcat")}：直接字符串；
     * {@code @CatNote(key="host", value="${host}")}：从环境变量中获取；
     * {@code @CatNote("bugcat")}：省略key，最终key与value值相同；
     * </pre>
     * */
    CatNote[] tags() default {};


    /**
     * 通过http调用时，CatServer类上的拦截器链。 <br>
     * CatServerInterceptor.class 表示启用全局的默认拦截器配置，最终该位置会被全局配置的拦截器替换； <br>
     * CatServerInterceptor.NoOp.class 表示关闭自定义拦截器和全局拦截器； <br>
     * CatServerInterceptor.GroupOff.class 关闭拦截器组。
     * <pre>
     * {@code @CatServer}：启用默认拦截器，默认拦截器在{@link CatServerConfiguration#getServerInterceptor()}指定；
     * {@code @CatServer(interceptors = {UserInterceptor.class, CatServerInterceptor.class})}：启用自定义拦截器和全局拦截器；
     * {@code @CatServer(interceptors = {UserInterceptor.class})}：仅启用自定义拦截器；
     * {@code @CatServer(interceptors = {UserInterceptor.GroupOff.class})}：关闭拦截器组；全局拦截器有效；
     * {@code @CatServer(interceptors = {UserInterceptor.NoOp.class})}：关闭全局拦截器和自定义拦截器；拦截器组有效；
     * {@code @CatServer(interceptors = {UserInterceptor.GroupOff.class})}：关闭拦截器组；全局拦截器有效；
     * {@code @CatServer(interceptors = {UserInterceptor.NoOp.class, UserInterceptor.GroupOff.class})}：关闭所有拦截器；
     * </pre>
     * */
    Class<? extends CatServerInterceptor>[] interceptors() default CatServerInterceptor.class;

    
    /**
     * 异常处理类
     * */
    Class<? extends CatResultHandler> resultHandler() default CatResultHandler.class;
    
}
