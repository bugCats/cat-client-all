package com.bugcat.catface.utils;

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

public class CatToosUtil {

    /**
     * spring EL解析式
     * */
    public static ExpressionParser parser = new SpelExpressionParser();


    /**
     * 获取扫描包路径
     * */
    public static String[] scanPackages(AnnotationMetadata metadata, AnnotationAttributes annoAttrs, String annoName){
        String[] pkgs = annoAttrs.getStringArray(annoName);
        if( pkgs.length == 1 && CatToosUtil.isBlank(pkgs[0]) ){//如果没有设置扫描包路径，取启动类路径
            StandardAnnotationMetadata annotationMetadata = (StandardAnnotationMetadata) metadata;
            Class<?> stratClass = annotationMetadata.getIntrospectedClass();    //启动类class
            String basePackage = stratClass.getPackage().getName();
            pkgs = new String[] {basePackage};  //获取启动类所在包路径
        }
        return pkgs;
    }
    
    
    public static String getAnnotationValue(AnnotatedElement element, Class<? extends Annotation>... anns){
        for(Class clazz : anns){
            Annotation annotation = element.getAnnotation(clazz);
            if( annotation != null ){
                Object value = AnnotationUtils.getValue(annotation);
                if( value != null && CatToosUtil.isNotBlank(value.toString()) ){
                    return value.toString();
                }
            }
        }
        return "";
    }

    
    public static boolean isBlank(String str){
        return str == null || "".equals(str.trim());
    }

    public static boolean isNotBlank(String str){
        return !isBlank(str);
    }

    public static String defaultIfBlank(String str, String def){
        return isNotBlank(str) ? str : def;
    }

    public static String toStringIfBlank(Object str, String def){
        return str != null ? str.toString() : def;
    }

    public static String uncapitalize(final String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }

        final int firstCodepoint = str.codePointAt(0);
        final int newCodePoint = Character.toLowerCase(firstCodepoint);
        if (firstCodepoint == newCodePoint) {
            return str;
        }

        final int newCodePoints[] = new int[strLen]; // cannot be longer than the char array
        int outOffset = 0;
        newCodePoints[outOffset++] = newCodePoint; // copy the first codepoint
        for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen; ) {
            final int codepoint = str.codePointAt(inOffset);
            newCodePoints[outOffset++] = codepoint; // copy the remaining ones
            inOffset += Character.charCount(codepoint);
        }
        return new String(newCodePoints, 0, outOffset);
    }

    public static boolean isSimpleClass (Class clz) {
        try {
            if ( clz.isPrimitive() ) {
                return true;
            } else if( clz == String.class ){
                return true;
            }
            return ((Class) clz.getField("TYPE").get(null)).isPrimitive();
        } catch ( Exception e ) {
            return false;
        }
    }

    public static String signature(Method method){
        return signature(method.getName(), method);
    }
    public static String signature(String name, Method method){
        StringBuilder sbr = new StringBuilder(300);
        Type[] types = method.getGenericParameterTypes();
        if( types != null && types.length > 0 ){
            for(Type type : types ){
                sbr.append("," + type.getTypeName());
            }
            sbr.deleteCharAt(0);
        }
        return name + "([" + sbr.toString() + "])";
    }
}
