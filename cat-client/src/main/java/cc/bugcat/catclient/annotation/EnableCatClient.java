package cc.bugcat.catclient.annotation;

import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.config.CatHttpRetryConfigurer;
import cc.bugcat.catclient.scanner.CatClientScannerRegistrar;
import cc.bugcat.catclient.spi.CatClientProvider;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**
 * 开启CatClient
 *
 * @author bugcat
 * */
@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({CatHttpRetryConfigurer.class, CatClientScannerRegistrar.class})
public @interface EnableCatClient {


    /**
     * 扫描包路径。默认是被注解类的包目录
     * */
    String[] value() default "";


    /**
     *
     * 指定客户端类。
     * <pre>
     *  1. classes是普通的interface类，则指定interface类创建客户端；
     *  2. classes是{@link CatClientProvider}的子类，并且子类方法上含有{@link CatClient}，视为批量声明客户端，其方法的返回对象为客户端；  
     * </pre>
     *
     * @see CatClientProvider
     * */
    Class[] classes() default {};



    /**
     * 默认值、以及配置项。
     *
     * 可以用来统一修改{@link CatClient}、{@link CatMethod}的默认值。
     *
     * 作用于全局。
     * */
    Class<? extends CatClientConfiguration> configuration() default CatClientConfiguration.class;


}
