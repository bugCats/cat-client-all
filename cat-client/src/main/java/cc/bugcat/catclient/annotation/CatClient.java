package cc.bugcat.catclient.annotation;

import cc.bugcat.catclient.spi.*;
import cc.bugcat.catclient.handler.CatMethodAopInterceptor;
import cc.bugcat.catclient.handler.CatLogsMod;
import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catface.annotation.CatNote;
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
     * 1、字面量："https://www.bugcat.cc"；
     * 2、读取配置文件值："${xxx.xxx}"；
     * 3、服务名，配合注册中心："http://myservername/ctx"；
     * */
    String host();


    /**
     * 由这个类负责创建：请求发送类、响应处理类、http工具类
     * */
    Class<? extends CatClientFactory> factory() default CatClientFactory.class;


    /**
     * http发送拦截器
     * 可以添加日志、修改入参签名、token等处理
     * */
    Class<? extends CatSendInterceptors> interceptor() default CatSendInterceptors.class;


    /**
     * 异常处理类，当接口发生http异常（40x、50x），执行的回调方法。
     *
     * 类似FeignClient的fallback。
     *
     * 1、Object.class：尝试使用interface默认方法，如果interface没有默认实现，再执行{@link CatResultProcessor#onHttpError}
     * 2、Void.class：关闭回调模式，直接执行{@link CatResultProcessor#onHttpError}
     * 3、其他class值，必须实现该interface。当发生异常后，执行实现类的对应方法。
     *
     * 如果在回调方法中，继续抛出异常，或者关闭回调模式，会执行{@link CatResultProcessor#onHttpError}进行处理。
     *
     * CatResultProcessor可以通过扩展后的CatClientFactory进行增强
     *
     * @see CatMethodAopInterceptor#intercept
     * */
    Class fallback() default Object.class;



    /**
     * http读值超时毫秒；-1 代表不限制
     * */
    int socket() default CatClientConfiguration.SOCKET;


    /**
     * http链接超时毫秒；-1 代表不限制
     * */
    int connect() default CatClientConfiguration.CONNECT;


    /**
     * 记录日志方案
     * */
    CatLogsMod logsMod() default CatLogsMod.Def;


    /**
     * 分组标记，
     * {@code @CatNote(key="name", value="bugcat")}：直接字符串；
     * {@code @CatNote(key="host", value="${host}")}：从环境变量中获取；
     * {@code @CatNote("bugcat")}：省略key，最终key与value值相同；
     * */
    CatNote[] tags() default {};


}
