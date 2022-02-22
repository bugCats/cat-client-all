package cc.bugcat.catclient.annotation;

import cc.bugcat.catclient.handler.CatLogsMod;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 定义http请求方法
 *
 * @author bugcat
 * */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping
public @interface CatMethod {


    /**
     * 具体的url
     * 1、字面量：/qq/972245132
     * 2、读取配置文件值：${xxx.xxx}
     * 3、支持uri类型参数：/qq/{pathVariable}
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
     * 追加的其他自定义参数、或标记。
     * <pre>
     * notes = {
     *      @CatNote(key="name", value="bugcat"),           直接字符串
     *      @CatNote(key="host", value="${host}"),          从环境变量中获取
     *      @CatNote(key="clazz", value="#{req.clazz}"),    从方法入参上获取对象.属性。此种方法，必须要为入参取别名
     *      @CatNote("bugcat")                              省略key，最终key与value值相同
     * }
     * </pre>
     *
     * 另外，方法的入参上别名，可使用：
     * <pre>
     *      @ModelAttribute("paramName")    声明post、get键值对对象，参数别名为paramName
     *      @PathVariable("paramName")      声明pathVariable类型参数，参数名为paramName
     *      @RequestParam("paramName")      声明键值对参数，参数名为paramName
     *      @RequestHeader("paramName")     声明请求头参数，请求头名为paramName
     *      @CatNote("paramName")           其他类型参数，结合@RequestBody使用
     * </pre>
     * */
    CatNote[] notes() default {};



    /**
     * http读值超时毫秒；
     * -1 不限；0 同当前interface配置；其他值，超时的毫秒数
     * */
    int socket() default 0;



    /**
     * http链接超时毫秒；
     * -1 不限；0 同当前interface配置；其他值，超时的毫秒数
     * */
    int connect() default 0;



    /**
     * 日志记录方案
     * Def：同当前interface配置
     * */
    CatLogsMod logsMod() default CatLogsMod.Def;


}
