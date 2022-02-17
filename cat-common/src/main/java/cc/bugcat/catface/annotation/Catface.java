package cc.bugcat.catface.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义精简模式
 *
 * 在feign-interface上添加，表示启用精简模式
 *
 *
 *
 * 开启精简模式，仅需要在interface上添加{@code @Catface}注解：
 *
 * Cat-Client客户端，使用DefineCatClients + @CatClient的方式，
 *
 * 将该interface注册为客户端，其内在所有的方法，都会视为API请求，
 *
 * 方法上的入参列表，会先转换成Map，Map键为‘arg0’~‘argX’按顺序自动生成，值为入参。
 *
 * 然后再转换成json字符串，post+json方式发起http请求。
 *
 * 请求的url为：配置的命名空间 + interface别名 + 方法名，
 *
 * 因此，这需要interface中的方法名不能相同，即不能存在重载方法。
 *
 *
 *
 * Cat-Server服务端，实现该interface，并且在实现类上添加@CatServer注解，服务端会将该interface注册为Controller角色。
 *
 * 处理方式与客户端类似，请求方式为post+json，url为命名空间+interface别名+方法名。
 *
 *
 *
 * 这样对于客户端、服务端而言：客户端调用interface方法 => 服务端实现interface，看上去好像是客户端通过interface，直接调用服务端的方法。
 *
 * -- 类似 dubbo 调用
 *
 * 不会感觉到底层实际通过http的调用。
 *
 *
 *
 *    客户端                    调用                      服务端
 *   服务消费者   ───────────────────────────────────>   服务提供者
 *      │                                                 │
 *      │                                                 │
 *      │                                                 │
 *      └──────────────  Service-interface                │
 *                              │                         │
 *                              │                         │
 *                              └─────────────────────────┤
 *                                                        │
 *                                                        │
 *                                                   Service 实现类
 *
 *
 *
 *
 * @author bugcat
 * */
@Target({ ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Catface {


    /**
     * interface类别名，默认是首字母小写。
     *
     * 最终url => /Catface.namespace()/Catface.value()/method.name
     * */
    String value() default "";


    /**
     * 命名空间
     * 统一的url前缀
     * */
    String namespace() default "";

}
