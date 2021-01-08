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
     * 指定interface，优先级高于包路径扫描
     * */
    Class[] classes() default {};


    /**
     * 各种默认值
     * */
    Class<? extends DefaultConfiguration> defaults() default DefaultConfiguration.class;

}
