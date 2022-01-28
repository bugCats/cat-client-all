package cc.bugcat.catclient.annotation;

import cc.bugcat.catclient.handler.AbstractCatResultProcessor;
import cc.bugcat.catclient.handler.CatMethodAopInterceptor;
import cc.bugcat.catclient.handler.CatLogsMod;
import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatMethodInterceptor;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;


/**
 *
 * 定义interface
 *
 * 在interface上添加注解，并且{@link EnableCatClient}启用了，会自动扫描装配。
 *
 * 属性的实际默认值，可以通过{@link CatClientConfiguration}修改
 *
 * @author bugcat
 * */
@Target({ ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface CatClient {


    /**
     * bean别名，默认为类名首字母小写
     * */
    @AliasFor(value = "value", annotation = Component.class)
    String value() default "";


    /**
     * 远程服务器地址：
     * 1. 字面量：https://www.bugcat.cc
     * 2. 读取配置文件值：${xxx.xxx}
     * 3. 服务名，配合注册中心：http://myservername/ctx
     * */
    String host();


    /**
     * 由这个类负责创建  请求发送类、响应处理类、http工具类
     * 如果需要扩展，请继承{@link CatClientFactory}，再在@CatClient中factory指向其扩展类
     * */
    Class<? extends CatClientFactory> factory() default CatClientFactory.class;


    /**
     * 发送拦截器
     * */
    Class<? extends CatMethodInterceptor> interceptor() default CatMethodInterceptor.class;

    /**
     * 异常处理类，当接口发生http异常（40x、50x），执行的回调方法。类似FeignClient的fallback
     * 异常处理类，必须实现被@CatClient标记的interface
     * 默认使用{@link AbstractCatResultProcessor#onHttpError}处理
     *
     * @see CatMethodAopInterceptor#intercept
     * */
    Class fallback() default Object.class;


    /**
     * 读值超时；默认值{@link CatClientConfiguration#socket}；-1 代表不限制
     * */
    int socket() default 0;


    /**
     * 链接超时；默认值{@link CatClientConfiguration#connect}；-1 代表不限制
     * */
    int connect() default 0;


    /**
     * 记录日志方案
     * */
    CatLogsMod logsMod() default CatLogsMod.Def;


    /**
     * 分组标记，配置重连分组
     * */
    String[] tags() default "";




}
