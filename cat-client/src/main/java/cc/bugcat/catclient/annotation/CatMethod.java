package cc.bugcat.catclient.annotation;

import cc.bugcat.catclient.handler.CatLogsMod;
import cc.bugcat.catface.annotation.CatNote;
import cc.bugcat.catface.annotation.CatNotes;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 定义http请求方法。
 * 如果在类上，则只有value属性生效！
 *
 * @author bugcat
 * */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
@RequestMapping
public @interface CatMethod {


    /**
     * 具体的url
     * <pre>
     *  1、字面量：/qq/972245132
     *  2、读取配置文件值：${xxx.xxx}
     *  3、支持uri类型参数：/qq/{pathVariable}  
     * </pre>
     * */
    @AliasFor(annotation = RequestMapping.class)
    String value() default "";


    /**
     * 请求方式：
     * 默认POST发送键值对(表单)
     * */
    @AliasFor(annotation = RequestMapping.class)
    RequestMethod method() default RequestMethod.POST;


    /**
     * 追加的其他自定义参数、或标记。会被{@link CatNotes}覆盖！当方法上有{@code @CatNotes}注解时，会忽略该属性。
     * <pre>
     *  {@code @CatNote(key="name", value="bugcat")}：直接字符串；
     *  {@code @CatNote(key="host", value="${host}")}：从环境变量中获取；
     *  {@code @CatNote("bugcat")}：省略key，最终key与value值相同；
     *  {@code @CatNote(key="clazz", value="#{req.clazz}")}：从方法入参上获取对象.属性。此种方法，必须要为入参取别名。 
     * </pre>
     * 
     * 另外，方法的入参上别名，可使用：
     * <pre>
     *  {@code @ModelAttribute("paramName")}：声明post、get键值对对象，参数别名为paramName；
     *  {@code @PathVariable("paramName")}：声明pathVariable类型参数，参数名为paramName；
     *  {@code @RequestParam("paramName")}：声明键值对参数，参数名为paramName；
     *  {@code @RequestHeader("paramName") }：声明请求头参数，请求头名为paramName；
     *  {@code @CatNote("paramName") }：其他类型参数，结合@RequestBody使用；
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
