package cc.bugcat.catserver.annotation;

import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.scanner.CatServerScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;



/**
 * 启用CatServer组件
 *
 * @author bugcat
 * */
@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CatServerScannerRegistrar.class)
public @interface EnableCatServer {


    /**
     * 扫描包路径
     * */
    String[] value() default "";


    /**
     * 或者指定interface
     * */
    Class[] classes() default {};


    /**
     * 默认值、以及配置项，作用于全局
     * */
    Class<? extends CatServerConfiguration> configuration() default CatServerConfiguration.class;

}
