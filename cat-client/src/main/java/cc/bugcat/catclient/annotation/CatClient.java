package cc.bugcat.catclient.annotation;

import cc.bugcat.catclient.handler.CatResultProcessor;
import cc.bugcat.catclient.handler.CatMethodAopInterceptor;
import cc.bugcat.catclient.handler.CatLogsMod;
import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.handler.DefineCatClients;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatMethodSendInterceptor;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;


/**
 *
 * 定义cat-client客户端
 *
 * 在interface上添加该注解，声明为Cat-Client客户端。
 *
 *  1、使用{@link EnableCatClient#value()}开启包自动扫描
 *  2、使用{@link EnableCatClient#classes()}指定客户端
 *  3、使用{@link EnableCatClient#classes()} + {@link DefineCatClients}批量定义
 *
 * 注解属性的实际默认值，可以通过{@link CatClientConfiguration}修改
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
     * 1、字面量：https://www.bugcat.cc
     * 2、读取配置文件值：${xxx.xxx}
     * 3、服务名，配合注册中心：http://myservername/ctx
     * */
    String host();


    /**
     * 由这个类负责创建：请求发送类、响应处理类、http工具类
     * */
    Class<? extends CatClientFactory> factory() default CatClientFactory.class;


    /**
     * http发送拦截器
     * */
    Class<? extends CatMethodSendInterceptor> interceptor() default CatMethodSendInterceptor.class;


    /**
     * 异常处理类，当接口发生http异常（40x、50x），执行的回调方法。类似FeignClient的fallback。
     * 默认使用{@link CatResultProcessor#onHttpError}处理
     * @see CatMethodAopInterceptor#intercept
     * */
    Class fallback() default Object.class;


    /**
     * 读值超时；-1 代表不限制
     * */
    int socket() default CatClientConfiguration.socket;


    /**
     * 链接超时；-1 代表不限制
     * */
    int connect() default CatClientConfiguration.connect;


    /**
     * 记录日志方案
     * */
    CatLogsMod logsMod() default CatLogsMod.Def;


    /**
     * 分组标记，配置重连分组
     * */
    String[] tags() default "";


}
