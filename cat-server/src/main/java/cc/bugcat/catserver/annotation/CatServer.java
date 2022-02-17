package cc.bugcat.catserver.annotation;


import cc.bugcat.catserver.spi.CatServerInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.*;


@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@ResponseBody
public @interface CatServer {

    /**
     * 别名
     * */
    String value() default "";


    /**
     * 分组标记，配置重连分组
     * */
    String[] tags() default "";

    /**
     * 拦截器
     * */
    Class<? extends CatServerInterceptor>[] interceptors() default CatServerInterceptor.class;

}
