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
 *
 * 例如：ResponesDTO<User>  =>  服务器响应对象为ResponesDTO，实际的业务数据，是ResponesDTO的一个属性。
 *
 * 配置{@code CatResponesWrapper#value()}，可以实现自动去掉外层的ResponesDTO，方法直接返回User对象
 *
 * 但是在实际http网络交互过程中，仍然有外层的ResponesDTO，对于业务层而言，ResponesDTO无感知的
 *
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
     *
     * */
    Class<? extends AbstractResponesWrapper> value() default AbstractResponesWrapper.Default.class;


}
