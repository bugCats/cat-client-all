package com.bugcat.catclient.annotation;

import com.bugcat.catclient.scanner.CatClientScannerRegistrar;
import com.bugcat.catclient.spi.DefaultConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;



/**
 * 开启CatClient
 * @author bugcat
 * */
@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CatClientScannerRegistrar.class)
public @interface EnableCatClient {
    
    
    /**
     * 扫描包路径
     * */
    String[] value() default "";
    
    
    /**
     * 指定interface，优先级高于value
     * 此项不为默认值情况下，会忽略value
     * */
    Class[] classes() default {};


    /**
     * 默认值、以及配置项
     * 可以用来统一修改{@link CatClient}{@link CatMethod}的默认值
     * 作用于全局
     * */
    Class<? extends DefaultConfiguration> defaults() default DefaultConfiguration.class;

}
