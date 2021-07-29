package cc.bugcat.catclient.annotation;


import java.lang.annotation.*;


/**
 * 
 * 1、为方法添加特殊标记
 * 
 * 2、为方法的入参起别名
 * 
 * @author bugcat
 * */
@Target({ ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CatNote {
    
    
    
    
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
