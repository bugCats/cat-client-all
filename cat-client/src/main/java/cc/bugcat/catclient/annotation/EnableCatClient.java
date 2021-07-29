package cc.bugcat.catclient.annotation;

import cc.bugcat.catclient.handler.CatClients;
import cc.bugcat.catclient.scanner.CatClientScannerRegistrar;
import cc.bugcat.catclient.spi.DefaultConfiguration;
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
     * 1、classes是普通的interface，指定interface创建
     * 
     * 2、classes是{@link CatClients}子类，并且类的方法上含有{@link CatClient}，视为声明客户端
     * @see CatClients
     * */
    Class[] classes() default {};


    /**
     * 默认值、以及配置项
     * 可以用来统一修改{@link CatClient}{@link CatMethod}的默认值
     * 作用于全局
     * */
    Class<? extends DefaultConfiguration> defaults() default DefaultConfiguration.class;

}
