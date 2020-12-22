package com.bugcat.catclient.annotation;

import com.bugcat.catclient.handler.RequestLogs;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 定义方法
 * @author bugcat
 * */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping
public @interface CatMethod {


    
    /**
     * 具体的 url   eg：/qq_972245132
     * 支持 ${xxx.xxx} 读取配置文件值
     * */
    @AliasFor(annotation = RequestMapping.class)
    String value() default "";

    
    
    /**
     * 请求方式：
     * 默认POST发送键值对
     * */
    @AliasFor(annotation = RequestMapping.class)
    RequestMethod method() default RequestMethod.POST;

    
    
    /**
     * 追加的其他自定义参数、标记。
     * */
    CatNote[] notes() default {};   // notes = {@CatNote(key="name", value="bugcat"), @CatNote(key="host", value="${host}", @CatNote(key="clazz", value="#{req.clazz}", @CatNote("bugcat"))}
    
    
    
    /**
     * 读值超时：
     * -1 不限；0 同当前类配置；其他值 超时的毫秒数
     * */
    int socket() default 0;
    
    
    
    /**
     * 链接超时：
     * -1 不限；0 同当前类配置；其他值 超时的毫秒数
     * */
    int connect() default 0;
    
    
    
    /**
     * 日志记录方案
     * Def：同当前类配置
     * */
    RequestLogs logs() default RequestLogs.Def;
    
    
    
}
