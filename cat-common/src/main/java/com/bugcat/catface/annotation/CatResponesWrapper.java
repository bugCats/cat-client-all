package com.bugcat.catface.annotation;

import com.bugcat.catface.spi.ResponesWrapper;

import java.lang.annotation.*;

/**
 * 定义全局的响应包裹类处理对象
 * 实际默认值，参考CatDefaultConfiguration
 * @author bugcat
 * */
@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CatResponesWrapper {


    /**
     * 统一的响应实体类包裹对象
     * */
    Class<? extends ResponesWrapper> value() default ResponesWrapper.Default.class;
    
    
}
