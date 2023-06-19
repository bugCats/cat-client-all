package cc.bugcat.catface.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 仅在catface模式生效
 * @author bugcat
 * */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(CatNotes.Group.class)
public @interface CatNotes {

    
    
    /**
     * 在catfa模式下为方法添加特殊标记
     * */
    CatNote[] value() default {};

    
    /**
     * 标记作用范围。
     * */
    Scope scope() default Scope.All;
    
            
    
    /**
     * 适用范围
     * */
    enum Scope {
        /**
         * 适用于客户端、服务端
         * */
        All,
        
        /**
         * 仅适用客户端
         * */
        Cilent,
        
        /**
         * 仅适用服务端
         * */
        Server;
        
        
        public boolean matcher(Scope scope){
            return Scope.All == this || scope == this;   
        }
    }


    /**
     * 多组
     * */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface Group {
        CatNotes[] value();
    }
    
}
