package cc.bugcat.catface.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义精简模式 
 * 
 * 包含了该注解之后，默认interface中所有方法均为api，并且方法名不能相同
 *
 * 默认所有请求都是 post + json 方式
 * 
 * url 通过 namespace + value + 方法名组成
 * 
 * @author bugcat
 * */
@Target({ ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Catface {

    String value() default "";  //别名，默认是interface类名。最终url => /namespace()/value()/method.name

    String namespace() default "";  //统一url前缀

}
