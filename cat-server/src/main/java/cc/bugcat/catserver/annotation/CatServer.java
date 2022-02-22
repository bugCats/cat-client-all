package cc.bugcat.catserver.annotation;


import cc.bugcat.catserver.spi.CatServerInterceptor;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.*;



/**
 * 标记在类上，使其暴露成API接口
 *
 * 该类必须实现类似于 feign-interface 的接口
 *
 * 也可以采用 cat-client
 *
 *
 * @author bugcat
 * */
@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@ResponseBody
public @interface CatServer {


    /**
     * 组件别名
     * */
    @AliasFor(value = "value", annotation = Component.class)
    String value() default "";


    /**
     * 其他标记。字面量："user"
     * */
    String[] tags() default "";


    /**
     * 通过http调用时，CatServer类上的拦截器链
     * CatServerInterceptor.class 表示启用全局的拦截器配置
     * CatServerInterceptor.Off.class 表示关闭所有拦截器
     * */
    Class<? extends CatServerInterceptor>[] interceptors() default CatServerInterceptor.class;


}
