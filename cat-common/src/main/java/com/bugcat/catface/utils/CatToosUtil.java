package com.bugcat.catface.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class CatToosUtil {

    /**
     * spring EL解析式
     * */
    public static ExpressionParser parser = new SpelExpressionParser();



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
        StringBuilder sbr = new StringBuilder(300);
        Type[] types = method.getGenericParameterTypes();
        if( types != null && types.length > 0 ){
            for(Type type : types ){
                sbr.append("," + type.getTypeName());
            }
            sbr.deleteCharAt(0);
        }
        return method.getName() + "([" + sbr.toString() + "])";
    }
}
