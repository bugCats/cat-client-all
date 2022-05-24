package cc.bugcat.catclient.annotation;

import cc.bugcat.catclient.handler.CatLogsMod;
import cc.bugcat.catface.annotation.CatNote;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@CatMethod(method = RequestMethod.GET)
public @interface CatGet {

    @AliasFor(annotation = CatMethod.class)
    String value() default "";

    
    @AliasFor(annotation = CatMethod.class)
    CatNote[] notes() default {};

    
    @AliasFor(annotation = CatMethod.class)
    int socket() default 0;

    
    @AliasFor(annotation = CatMethod.class)
    int connect() default 0;


    @AliasFor(annotation = CatMethod.class)
    CatLogsMod logsMod() default CatLogsMod.Def;

}
