package com.bugcat.catface.annotation;

import com.bugcat.catface.spi.ResponesWrapper;

import java.lang.annotation.*;

/**
 * 定义全局的响应包装器类处理对象
 * {@link ResponesWrapper} 
 * 
 * @author bugcat
 * */
@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CatResponesWrapper {


    /**
     * 统一的响应实体类包装器
     * */
    Class<? extends ResponesWrapper> value() default ResponesWrapper.Default.class;
    
    
}
