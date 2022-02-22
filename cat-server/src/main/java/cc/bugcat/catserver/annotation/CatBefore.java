package cc.bugcat.catserver.annotation;


import cc.bugcat.catserver.spi.CatParameterResolver;

import java.lang.annotation.*;

/**
 * CatServer类方法上自定义入参处理器
 * */
@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CatBefore {


    /**
     * 自定义参数预处理
     * */
    Class<? extends CatParameterResolver> value();


}
