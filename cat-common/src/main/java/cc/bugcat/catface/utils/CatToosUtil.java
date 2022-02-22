package cc.bugcat.catface.utils;

import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.catface.annotation.Catface;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 *
 * @author bugcat
 * */
public class CatToosUtil{

    public final static String GROUP_ID = "cc.bugcat";

    public final static String INTERFACE_ATTRIBUTES_CATFACE = "catface";
    public final static String INTERFACE_ATTRIBUTES_WRAPPER = "wrapper";

    public final static String INTERFACE_ATTRIBUTES_SERVICE_NAME = "serviceName";
    public final static String INTERFACE_ATTRIBUTES_DEPENDS = "interfaceDepend";


    /**
     * spring EL表达式：#{arg.name}
     * 使用方法入参，计算表达式
     * */
    public static ExpressionParser parser = new SpelExpressionParser();


    /**
     * Object.class中的方法
     * */
    private static final Set<String> objectDefaultMethod;
    static {
        Set<String> methodSet = new HashSet<>();
        for ( Method method : Object.class.getDeclaredMethods() ) {
            methodSet.add(signature(method));
        }
        objectDefaultMethod = Collections.unmodifiableSet(methodSet);
    }



    /**
     * 进入异常流程时，存储异常信息
     * */
    private static ThreadLocal<Throwable> threadLocal = new ThreadLocal<>();

    /**
     * 在异常回调流程中获取异常信息
     * */
    public static Throwable getException(){
        return threadLocal.get();
    }

    /**
     * 在异常回调流程中再次抛出
     * */
    public static void throwException(){
        Throwable throwable = threadLocal.get();
        throw new RuntimeException(throwable);
    }

    public static void setException(Throwable throwable){
        threadLocal.set(throwable);
    }
    public static void removeException(){
        threadLocal.remove();
    }



    /**
     * 得到原始异常
     * */
    public static Throwable getCause(Throwable throwable){
        Throwable error = throwable;
        while ( error.getCause() != null ) {
            error = error.getCause();
        }
        return error;
    }


    /**
     * 获取扫描包路径，默认为启动类所在包路径
     * */
    public static String[] scanPackages(AnnotationMetadata metadata, AnnotationAttributes annoAttrs) {
        String[] pkgs = annoAttrs.getStringArray("value");
        if ( pkgs.length == 1 && CatToosUtil.isBlank(pkgs[0]) ) {//如果没有设置扫描包路径，取启动类路径
            StandardAnnotationMetadata annotationMetadata = (StandardAnnotationMetadata) metadata;
            Class startClass = annotationMetadata.getIntrospectedClass();    //启动类class
            String basePackage = startClass.getPackage().getName();
            pkgs = new String[]{basePackage};  //获取启动类所在包路径
        }
        return pkgs;
    }

    /**
     * 按顺序，获取第一个存在的注解的value值
     * */
    public static String getAnnotationValue(AnnotatedElement element, Class<? extends Annotation>... anns) {
        for ( Class clazz : anns ) {
            Annotation annotation = element.getAnnotation(clazz);
            if ( annotation != null ) {
                Object value = AnnotationUtils.getValue(annotation);
                if ( value != null && CatToosUtil.isNotBlank(value.toString()) ) {
                    return value.toString();
                }
            }
        }
        return "";
    }


    public static boolean isBlank(String str) {
        int strLen = 0;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String defaultIfBlank(String str, String def) {
        return isNotBlank(str) ? str : def;
    }

    public static String toStringIfBlank(Object str, String def) {
        return str != null ? str.toString() : def;
    }


    /**
     * 是否为object内置方法
     */
    public static boolean isObjectMethod(Method method) {
        return isObjectMethod(signature(method));
    }
    public static boolean isObjectMethod(String sign) {
        return objectDefaultMethod.contains(sign);
    }


    /**
     * 首字母小写
     * */
    public static String uncapitalize(final String str) {
        return capitalize(str, String::toLowerCase);
    }

    /**
     * 首字母大写
     * */
    public static String capitalize(final String str) {
        return capitalize(str, String::toUpperCase);
    }

    private static String capitalize(final String str, Function<String, String> func) {
        int strLen;
        if ( str == null || (strLen = str.length()) == 0 ) {
            return str;
        }
        char[] chars = str.toCharArray();
        if( strLen > 1 ){
            return func.apply(String.valueOf(chars[0])) + new String(chars, 1, chars.length - 1);
        } else {
            return func.apply(String.valueOf(chars[0]));
        }
    }


    /**
     * 判断否为基础数据类型
     * */
    public static boolean isSimpleClass(Class clazz) {
        if ( clazz.isPrimitive() || clazz == String.class ) {
            return true;
        } else {
            try {
                return ((Class) clazz.getField("TYPE").get(null)).isPrimitive();
            } catch ( Exception e ) {
                return false;
            }
        }
    }


    /**
     * 过滤堆栈
     * */
    public static StackTraceElement[] filterStackTrace(Throwable throwable, String groupId){
        StackTraceElement[] stackTraces = throwable.getStackTrace();
        return Arrays.stream(stackTraces).filter(stackTrace -> stackTrace.getClassName().contains(groupId))
                .toArray(StackTraceElement[]::new);
    }


    /**
     * 方法签名：
     * @return method.name([parameterType, parameterType])
     * */
    public static String signature(Method method) {
        StringBuilder sbr = new StringBuilder(300);
        Type[] types = method.getGenericParameterTypes();
        if ( types != null && types.length > 0 ) {
            for ( Type type : types ) {
                sbr.append("," + type.getTypeName());
            }
            sbr.deleteCharAt(0);
        }
        return method.getName() + "([" + sbr.toString() + "])";
    }


    /**
     * 从interface上获取注解
     * */
    public static Map<String, Object> getAttributes(Class inter) {
        Map<String, Object> paramMap = new HashMap<>();
        CatResponesWrapper wrapper = responesWrap(inter, CatResponesWrapper.class);
        paramMap.put(INTERFACE_ATTRIBUTES_WRAPPER, wrapper);
        Catface catface = responesWrap(inter, Catface.class);
        paramMap.put(INTERFACE_ATTRIBUTES_CATFACE, catface);
        return paramMap;
    }

    /**
     * 递归遍历interface、以及父类，获取第一次出现的annotationType注解
     * */
    public static <A extends Annotation> A responesWrap(Class inter, Class<A> annotationType) {
        A annotation = AnnotationUtils.findAnnotation(inter, annotationType);
        if ( annotation == null ) {
            for ( Class clazz : inter.getInterfaces() ) {
                annotation = responesWrap(clazz, annotationType);
                if ( annotation != null ) {
                    return annotation;
                }
            }
        }
        return annotation;
    }

    /**
     * 精简模式下，获取url
     * */
    public static String getDefaultRequestUrl(Catface catface, String serviceName, Method method) {
        String namespace = "";
        String aliasValue = CatToosUtil.uncapitalize(serviceName);
        if ( catface != null ) {
            namespace = CatToosUtil.isBlank(catface.namespace()) ? "" : "/" + catface.namespace();
            aliasValue = CatToosUtil.isBlank(catface.value()) ? aliasValue : catface.value();
        }
        String path = namespace + "/" + aliasValue + "/" + method.getName();
        return path;
    }


    /**
     * 从contrasts去掉standar，如果均没有则返回defaultValue
     * */
    public static <T> T comparator(Object standar, List<Object> contrasts, T defaultValue){
        for ( Object contrast : contrasts ) {
            if ( !standar.equals(contrast) ) {
                return (T) contrast;
            }
        }
        return defaultValue;
    }


}
