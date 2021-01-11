package com.bugcat.catclient.annotation;

import com.bugcat.catclient.handler.CatMethodInterceptor;
import com.bugcat.catclient.handler.RequestLogs;
import com.bugcat.catclient.handler.ResultProcessor;
import com.bugcat.catclient.spi.CatClientFactory;
import com.bugcat.catclient.spi.DefaultConfiguration;
import com.bugcat.catclient.spi.DefaultMethodInterceptor;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;


/**
 * 
 * 定义interface
 * 
 * 属性的实际默认值，可以通过{@link DefaultConfiguration}修改 
 * 
 * @author bugcat
 * */
@Target({ ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface CatClient {
    
    
    /**
     * bean别名
     * */
    @AliasFor(value = "value", annotation = Component.class)
    String value() default "";
    
    
    /**
     * 远程服务器地址 eg：https://www.bugcat.com
     * 支持 ${xxx.xxx} 读取配置文件值
     * */
    String host();
    
    
    /**
     * 由这个类负责创建  请求发送类、响应处理类、http工具类
     * 如果需要扩展，请继承{@link CatClientFactory}，再在@CatClient中指向其扩展类
     * */
    Class<? extends CatClientFactory> factory() default CatClientFactory.class;
    
    
    /**
     * 异常处理类，当接口发生http异常（40x、50x），执行的回调方法。类似FeignClient的fallback
     * 异常处理类，必须实现被@CatClient标记的interface
     * 默认使用{@link ResultProcessor#onHttpError}处理
     * @see DefaultMethodInterceptor#intercept
     * */
    Class fallback() default Object.class;
    
    
    /**
     * 读值超时；默认值{@link DefaultConfiguration#socket}；-1 代表不限制
     * */
    int socket() default 0;
    
    
    /**
     * 链接超时；默认值{@link DefaultConfiguration#connect}；-1 代表不限制
     * */
    int connect() default 0;
    
    
    /**
     * 记录日志方案
     * */
    RequestLogs logs() default RequestLogs.Def;
 
    
    /**
     * 分组标记，配置重连分组
     * */
    String[] tags() default "";
    
    
    /**
     * 动态生成的http请求代理类
     * 一般情况无需修改
     * */
    Class<? extends CatMethodInterceptor> interceptor() default DefaultMethodInterceptor.class;
    
}
