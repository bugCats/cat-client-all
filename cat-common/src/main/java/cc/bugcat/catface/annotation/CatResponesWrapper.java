package cc.bugcat.catface.annotation;

import cc.bugcat.catface.spi.AbstractResponesWrapper;

import java.lang.annotation.*;

/**
 * 定义全局的响应包装器类处理对象
 *
 * 在Cat-Client客户端，表示自动拆包装器类
 *
 * 在Cat-Server服务端，表示响应自动加包装器
 *
 * {@link AbstractResponesWrapper}
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
    Class<? extends AbstractResponesWrapper> value() default AbstractResponesWrapper.Default.class;


}
