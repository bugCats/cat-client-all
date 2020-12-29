package com.bugcat.catclient.annotation;

import com.bugcat.catclient.handler.RequestLogs;
import com.bugcat.catclient.spi.CatClientFactory;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;


/**
 * 定义类
 * 实际默认值，参考CatDefaultConfiguration
 * @author bugcat
 * */
@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface CatClient {
    
    
    /**
     * 别名
     * */
    @AliasFor(value = "value", annotation = Component.class)
    String value() default "";
    
    
    /**
     * 远程服务器地址 eg：https://www.bugcat.com
     * 支持 ${xxx.xxx} 读取配置文件值
     * */
    String host();
    
    
    /**
     * 创建catClient工厂类，由这个类负责创建请求发送类，和响应处理类
     * 如果需要扩展，请继承CatClientFactory，再在@CatClient中指向其扩展类
     * */
    Class<? extends CatClientFactory> factory() default CatClientFactory.class;
    
    
    /**
     * 异常处理类，当接口发生http异常（40x、50x），执行的回调方法。类似FeignClient的fallback
     * 异常处理类，必须实现被@CatClient标记的interface
     * 默认使用 ResultProcessor.httpError 处理
     * */
    Class fallback() default Object.class;
    
    
    
    /**
     * 读值超时，默认CatDefaultConfiguration#socket；-1 代表不限制
     * */
    int socket() default 0;
    
    
    /**
     * 链接超时，默认CatDefaultConfiguration#connect；-1 代表不限制
     * */
    int connect() default 0;
    
    
    /**
     * 记录日志方案
     * */
    RequestLogs logs() default RequestLogs.Def;
    
}
