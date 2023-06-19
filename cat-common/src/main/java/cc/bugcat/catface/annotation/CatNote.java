package cc.bugcat.catface.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 1、为方法添加特殊标记
 * 2、为方法的入参起别名
 *
 * @author bugcat
 * */
@Target({ ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CatNote {




    /**
     * 键值对：键
     * */
    String key() default "";


    /**
     * 键值对：值
     * 1、读取配置文件值：${xxx.xxx}
     * 2、读取方法上入参的属性值：#{xxx.xxx}
     * */
    String value();


}
