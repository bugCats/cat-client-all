package cc.bugcat.catface.utils;

import cc.bugcat.catface.annotation.CatNote;
import cc.bugcat.catface.annotation.CatNotes;
import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.catface.handler.CatApiInfo;
import cc.bugcat.catface.spi.CatClientBridge;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.classreading.AnnotationMetadataReadingVisitor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
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
     * Object.class中的方法
     * */
    private static final Set<String> objectDefaultMethod;

    /**
     * 兼容客户端扩展组件
     * */
    private static final CatClientBridge clientBridge;

    /**
     * Throwable 错误描述字段
     * */
    private static final Field messageField;


    static {
        Set<String> methodSet = new HashSet<>();
        for ( Method method : Object.class.getDeclaredMethods() ) {
            methodSet.add(signature(method));
        }
        objectDefaultMethod = Collections.unmodifiableSet(methodSet);

        final ServiceLoader<CatClientBridge> loaders = ServiceLoader.load(CatClientBridge.class);
        final Iterator<CatClientBridge> iterators = loaders.iterator();
        if (iterators.hasNext()) {
            clientBridge = iterators.next();
        } else {
            clientBridge = new CatClientBridge() {};
        }

        messageField = ReflectionUtils.findField(Throwable.class, "detailMessage");
        ReflectionUtils.makeAccessible(messageField);
    }



    /**
     * 得到原始异常
     * */
    public static Throwable getCause(final Throwable throwable){
        Throwable last = throwable;
        for( Throwable temp = null; (temp = getTargetException(last)) != null; ){
            last = temp;
        }
        ReflectionUtils.setField(messageField, last, throwable.getMessage());
        return last;
    }

    private static Throwable getTargetException(Throwable throwable){
        if( throwable == null ){
            return null;
        }
        if( throwable instanceof InvocationTargetException ){
            return ((InvocationTargetException) throwable).getTargetException();
        } else {
            return throwable.getCause();
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
    public static String defaultIfBlank(String str, Supplier<String> supplier) {
        return isNotBlank(str) ? str : supplier.get();
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
     * 执行父类方法
     * */
    public static MethodInterceptor superObjectInterceptor(){
        return new SuperObjectInterceptor();
    }

    /**
     * 执行父类方法，默认的拦截器；
     * */
    private static class SuperObjectInterceptor implements MethodInterceptor {
        @Override
        public Object intercept (Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            return methodProxy.invokeSuper(target, args);
        }
    }

    /**
     * 在服务端模块，需要使用客户端一些方法；
     * 如果服务端模块引入了cat-client，那么直接使用；否则返回默认的处理方式；
     * */
    public static CatClientBridge getClientBridge() {
        return clientBridge;
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


    /**
     * 从interface上获取注解
     * */
    public static void parseInterfaceAttributes(Class inter, CatApiInfo apiInfo) {
        String basePath = clientBridge.findBasePath(inter);
        CatResponesWrapper wrapper = findAnnotation(inter, CatResponesWrapper.class);
        Catface catface = findAnnotation(inter, Catface.class);
        apiInfo.setCatface(catface);
        apiInfo.setWrapper(wrapper);
        apiInfo.setBasePath(basePath);
    }


    /**
     * 递归遍历interface、以及父类，获取第一次出现的annotationType注解
     * */
    public static <A extends Annotation> A findAnnotation(Class inter, Class<A> annotationType) {
        A annotation = AnnotationUtils.findAnnotation(inter, annotationType);
        if ( annotation == null ) {
            for ( Class clazz : inter.getInterfaces() ) {
                annotation = findAnnotation(clazz, annotationType);
                if ( annotation != null ) {
                    return annotation;
                }
            }
        }
        return annotation;
    }

    /**
     * 精简模式下，获取url、请求方式；
     * 兼容使用@RequestMapping、@CatMethod注解；
     * */
    public static void parseDefaultRequest(Catface catface, String serviceName, Method method, Map<String, Object> methodAttrs) {
        String namespace = "";
        String aliasValue = CatToosUtil.uncapitalize(serviceName);
        String methodPath = null;
        RequestMethod requestType = null;
        
        if ( catface != null ) {
            namespace = CatToosUtil.isBlank(catface.namespace()) ? "" : "/" + catface.namespace();
            aliasValue = CatToosUtil.isBlank(catface.value()) ? aliasValue : catface.value();
        }
        // 在精简模式下，为方法取别名
        RequestMapping mapping = clientBridge.findMethodPath(method);
        if( mapping != null ){
            String value = mapping.value()[0];
            methodPath = value.startsWith("/") ? value.substring(1) : value;
            requestType = mapping.method()[0];
        } else {
            methodPath = method.getName();
            requestType = RequestMethod.POST;
        }
        String requestPath = namespace + "/" + aliasValue + "/" + methodPath;

        methodAttrs.put("value", requestPath);
        methodAttrs.put("method", requestType);
    }

    
    
    public static CatNote[] getCatNotes(Method method, CatNotes.Scope scope){
        CatNotes[] notes = null;
        CatNotes.Group noteGroup = AnnotationUtils.findAnnotation(method, CatNotes.Group.class);
        if( noteGroup != null  ){
            notes = noteGroup.value();
        } else {
            CatNotes catNote = AnnotationUtils.findAnnotation(method, CatNotes.class);
            if( catNote != null ){
                notes = new CatNotes[]{catNote};
            }
        }
        if( notes == null ){
            return null;
        }
        
        List<CatNote[]> noteList = new ArrayList<>(notes.length);
        int length = 0;
        for ( CatNotes note : notes ) {
            if( note.scope().matcher(scope) ){
                CatNote[] values = note.value();
                length += values.length;
                noteList.add(values);
            }
        }
        CatNote[] note = new CatNote[length];
        length = 0;
        for ( CatNote[] catNotes : noteList ) {
            for ( CatNote catNote : catNotes ) {
                note[length ++] = catNote;
            }
        }
        return note;
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



}
