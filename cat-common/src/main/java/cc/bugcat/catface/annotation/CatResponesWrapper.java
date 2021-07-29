package cc.bugcat.catface.annotation;

import cc.bugcat.catface.spi.AbstractResponesWrapper;

import java.lang.annotation.*;

/**
 * 定义全局的响应包装器类处理对象
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
