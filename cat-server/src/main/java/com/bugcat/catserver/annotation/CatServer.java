package com.bugcat.catserver.annotation;


import com.bugcat.catface.spi.ResponesWrapper;
import com.bugcat.catserver.spi.CatInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.*;

@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@ResponseBody
@Inherited
public @interface CatServer {

    /**
     * 别名
     * */
    String value() default "";


    /**
     * 统一的响应实体类包裹对象
     * */
    Class<? extends ResponesWrapper> wrapper() default ResponesWrapper.Default.class;
    
    /**
     * 拦截器
     * */
    Class<? extends CatInterceptor>[] handers() default CatInterceptor.class;
    
}
