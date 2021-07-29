package cc.bugcat.catserver.annotation;

import cc.bugcat.catserver.scanner.CatServerScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;



/**
 * 开启CatServer
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

}
