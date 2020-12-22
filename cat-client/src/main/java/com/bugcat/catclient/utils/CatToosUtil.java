package com.bugcat.catclient.utils;

import com.bugcat.catclient.annotation.CatClient;
import com.bugcat.catclient.beanInfos.CatClientInfo;
import com.bugcat.catclient.scanner.CatClientInfoFactoryBean;
import com.bugcat.catclient.spi.CatDefaultConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author bugcat
 * */
public class CatToosUtil implements ApplicationContextAware {

    
    public static final Pattern keyPat1 = Pattern.compile("^\\$\\{(.+)\\}$");
    public static final Pattern keyPat2 = Pattern.compile("^\\#\\{(.+)\\}$");
    
    /**
     * spring EL解析式
     * */
    public static ExpressionParser parser = new SpelExpressionParser();
    
    
    private static Map<Class, Object> catClinetMap = new ConcurrentHashMap<>();

    private static ApplicationContext context;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
    
    
    public static <T> T getBean(Class<T> clazz){
        try {
            return context.getBean(clazz);
        } catch ( Exception e ) {
            return (T) catClinetMap.get(clazz);
        }
    }
    
    /**
     * 注册bean
     * */
    public static void registerBean(Class type, Object bean){
        catClinetMap.putIfAbsent(type, bean);
    }
    
    /**
     * 通过静态方法创建
     * */
    public static <T> T proxy(Class<T> inter){
        return proxy(inter, new Properties());
    }
    
    /**
     * 通过静态方法创建，包含读取环境变量情况
     * */
    public static <T> T proxy(Class<T> inter, Properties properties){
        if( catClinetMap.containsKey(inter) ){
            return (T) catClinetMap.get(inter);
        }
        CatDefaultConfiguration config = (CatDefaultConfiguration) properties.getOrDefault(CatDefaultConfiguration.class, getBean(CatDefaultConfiguration.class));
        if( config == null ){
            config = new CatDefaultConfiguration();
            registerBean(CatDefaultConfiguration.class, config);
        }
        CatClient catClient = inter.getAnnotation(CatClient.class);
        AnnotationAttributes attr = (AnnotationAttributes) AnnotationUtils.getAnnotationAttributes(catClient);
        ToosProperty prop = new ToosProperty(properties);
        attr.put("beanName", inter.getSimpleName());
        attr.put("config", config);
        CatClientInfo clientInfo = new CatClientInfo(attr, prop);
        T bean = CatClientInfoFactoryBean.createCatClients(inter, clientInfo, prop);
        registerBean(inter, bean);
        return bean;
    }
    
    
    
    private static class ToosProperty extends Properties {

        private Properties prop;

        public ToosProperty(Properties prop) {
            this.prop = prop;
        }

        @Override
        public String getProperty(String key) {
            return getProperty(key, null);
        }
        @Override
        public String getProperty(String key, String defaultValue) {
            if( key.startsWith("${") ){
                Matcher matcher = keyPat1.matcher(key);
                if ( matcher.find() ) {
                    String[] keys = matcher.group(1).split(":");
                    if( keys.length > 1 && defaultValue == null ){
                        defaultValue = keys[1];
                    }
                    key = keys[0];
                }
            }
            String value = prop.getProperty(key, defaultValue);
            return value == null ? key : value;
        }
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