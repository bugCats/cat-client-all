package cc.bugcat.catface.utils;

import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.catface.handler.CatApiInfo;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.classreading.AnnotationMetadataReadingVisitor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 *
 * @author bugcat
 * */
public class CatToosUtil {

    public final static String GROUP_ID = "cc.bugcat";

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
    public static String[] scanPackages(AnnotationMetadata annotationMetadata, AnnotationAttributes annoAttrs) {
        String[] pkgs = annoAttrs.getStringArray("value");
        if ( pkgs.length == 1 && CatToosUtil.isBlank(pkgs[0] ) ) {//如果没有设置扫描包路径，取启动类路径
            Class startClass = null;
            if ( annotationMetadata instanceof AnnotationMetadataReadingVisitor ){ //非启动类，在一般spring组件上
                AnnotationMetadataReadingVisitor metadata = (AnnotationMetadataReadingVisitor) annotationMetadata;
                try { startClass = metadata.getClass().getClassLoader().loadClass(metadata.getClassName()); } catch ( Exception ex ) { }
            } else { //在启动类上
                StandardAnnotationMetadata metadata = (StandardAnnotationMetadata) annotationMetadata;
                startClass = metadata.getIntrospectedClass();    //启动类class
            }
            String basePackage = startClass.getPackage().getName();
            pkgs = new String[]{basePackage};  //获取启动类所在包路径
        }
        String[] scan = new String[pkgs.length + 1];
        scan[0] = GROUP_ID;
        System.arraycopy(pkgs, 0, scan, 1, pkgs.length);
        return scan;
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
        return method.getName() + "([" + sbr + "])";
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
     * 从interface上获取注解
     * */
    public static <T extends CatApiInfo> T getAttributes(Class inter, Supplier<T> supplier) {
        CatResponesWrapper wrapper = responesWrap(inter, CatResponesWrapper.class);
        Catface catface = responesWrap(inter, Catface.class);
        CatApiInfo apiInfo = supplier.get();
        apiInfo.setCatface(catface);
        apiInfo.setWrapper(wrapper);
        return (T) apiInfo;
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

    /**
     * 一般Map转MultiValueMap
     * */
    public static MultiValueMap<String, Object> toMultiValueMap(Map<String, Object> map){
        if( map == null || map.size() == 0 ){
            return new LinkedMultiValueMap<>();
        }
        MultiValueMap<String, Object> valueMap = new LinkedMultiValueMap<>(map.size() * 2);
        map.forEach((key, value) -> {
            if( value == null ){
                valueMap.add(key, "");
            } else {
                if( value instanceof List ){
                    for(Object val : (List<Object>) value){
                        valueMap.add(key, val == null ? null : val.toString());
                    }
                } else {
                    valueMap.add(key, value == null ? null : value.toString());
                }
            }
        });
        return valueMap;
    }
    
    
    
    /**
     * 适配Spring环境变量为Properties
     * */
    public static Properties envProperty(Environment environment){
        return new EnvironmentProperty(environment);
    }
    /**
     * 适配自定义环境变量为Properties
     * */
    public static Properties envProperty(Properties properties){
        if( properties instanceof EnvironmentProperty){
            return properties;
        } else {
            StandardEnvironment source = new StandardEnvironment();
            source.getPropertySources().addLast(new PropertiesPropertySource("catToolsProperty", properties));
            return new EnvironmentProperty(source);
        }
    }


    /**
     * 环境变量适配
     * */    
    private static class EnvironmentProperty extends Properties {

        private final PropertyResolver property;
        private EnvironmentProperty(PropertyResolver property) {
            this.property = property;
        }

        /**
         * key 类似于 ${demo.remoteApi}
         * */
        @Override
        public String getProperty(String key) {
            return getProperty(key, null);
        }

        @Override
        public String getProperty(String key, String defaultValue) {
            String value = property.resolvePlaceholders(key);
            return defaultValue != null && key.equals(value) ? defaultValue : value;
        }
    }


}
