package com.bugcat.catclient.annotation;


import java.lang.annotation.*;


/**
 * 为方法添加特殊标记
 * @author bugcat
 * */
@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CatNote {

    /**
     * 
     * 对方法的额外标记
     * 
     * */
    
    
    
    
    /**
     * 键值对：键
     * */
    String key() default "";
    
    
    /**
     * 键值对：值
     * 支持 ${xxx.xxx} 读取配置文件值
     * 支持 #{xxx.xxx} 读取方法上入参的属性值
     * */
    String value();
    
    
}
