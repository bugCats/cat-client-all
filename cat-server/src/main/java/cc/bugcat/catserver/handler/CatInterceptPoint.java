package cc.bugcat.catserver.handler;

import cc.bugcat.catface.handler.EnvironmentAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


/**
 * 访问CatServer的拦截器。
 * 对于每次请求为单例
 *
 * @author bugcat
 * */
public final class CatInterceptPoint {

    /**
     * 原interface方法上的注解属性
     * */
    private final Map<Class, Map<String, Object>> annAttrMap = new HashMap<>();

    /**
     * 自定义属性
     * */
    private final Map<String, Object> attributesMap = new HashMap<>();


    private final HttpServletRequest request;
    private final HttpServletResponse response;

    /**
     * CatServer注解信息
     * */
    private final CatServerInfo serverInfo;

    /**
     * 原interface的方法
     * */
    private final CatMethodInfo methodInfo;
    
    /**
     * 访问的CatServer类对象
     * */
    private final Object target;

    /**
     * 处理后的方法入参
     * */
    private final Object[] arguments;
    
    /**
     * {@code @CarNotes}
     * */
    private final Map<String, Object> noteMap;


    private CatInterceptPoint(Builder builder) {
        this.request = builder.request;
        this.response = builder.response;
        this.serverInfo = builder.serverInfo;
        this.methodInfo = builder.methodInfo;
        this.target = builder.target;
        this.arguments = builder.arguments;
        this.noteMap = builder.noteMap;
    }

    /**
     * 获取到CatServer类上的注解
     * */
    public <A extends Annotation> A getAnnotations(Class<A> annotationClass){
        return serverInfo.getServerClass().getAnnotation(annotationClass);
    }
    /**
     * 获取方法上的注解
     * */
    public Map<String, Object> getMethodAnnotations(Class annotationClass){
        Map<String, Object> attr = annAttrMap.get(annotationClass);
        if( attr == null ){
            Map<String, Object> map = methodInfo.getInterfaceMethod().getAnnotationAttributes(annotationClass.getName());
            if( map == null ){
                map = new HashMap<>();
            }
            attr = Collections.unmodifiableMap(map);
            annAttrMap.put(annotationClass, attr);
        }
        return attr;
    }
    public CatInterceptPoint settAttribute(String name, Object value){
        attributesMap.put(name, value);
        return this;
    }
    public <T> T getAttribute(String name, Class<T> clazz){
        return (T) attributesMap.get(name);
    }
    public <T> T getAttribute(String name, Supplier<T> supplier){
        Object value = attributesMap.get(name);
        return value != null ? (T) value : supplier.get();
    }
    public Map<String, Object> getAttributesMap() {
        return attributesMap;
    }

    
    public Class<?> getServerClass(){
        return serverInfo.getServerClass();
    }
    public Map<String, String> getServerTagsMap() {
        return serverInfo.getTagsMap();
    }

    public HttpServletRequest getRequest() {
        return request;
    }
    public HttpServletResponse getResponse() {
        return response;
    }
    public Object getTarget() {
        return target;
    }
    
    /**
     * url映射调用的方法。为原始interface的方法
     * */
    public Method getMethod() {
        return methodInfo.getInterfaceMethod().getIntrospectedMethod();
    }
    
    public Object[] getArguments() {
        return arguments;
    }
    public Map<String, Object> getNoteMap() {
        return noteMap;
    }

    protected static Builder builder(){
        return new Builder();
    }


    protected static class Builder {

        private HttpServletRequest request;
        private HttpServletResponse response;
        private CatServerInfo serverInfo;
        private Object target;
        private Object[] arguments;
        private CatMethodInfo methodInfo;
        private Map<String, Object> noteMap;
        
        public Builder request(HttpServletRequest request) {
            this.request = request;
            return this;
        }

        public Builder response(HttpServletResponse response) {
            this.response = response;
            return this;
        }

        public Builder serverInfo(CatServerInfo serverInfo) {
            this.serverInfo = serverInfo;
            return this;
        }

        public Builder methodInfo(CatMethodInfo methodInfo) {
            this.methodInfo = methodInfo;
            return this;
        }
        
        public Builder target(Object target) {
            this.target = target;
            return this;
        }

        public Builder arguments(Object[] arguments) {
            this.arguments = arguments;
            return this;
        }

 

        public CatInterceptPoint build(){

            // 将入参数组args，转换成： 参数名->入参    此时argsMap中一定不包含SendProcessor
            Map<String, Object> argsMap = new HashMap<>();
            methodInfo.getParamIndex().forEach((key, value) -> {
                argsMap.put(key, arguments[value]);  // value 等于该参数在方法上出现的索引值
            });
            
            CatServerDepend serverDepend = serverInfo.getServerDepend();
            EnvironmentAdapter envProp = serverDepend.getEnvironmentAdapter();
            EnvironmentAdapter newAdapter = EnvironmentAdapter.newAdapter(envProp, argsMap);

            this.noteMap = new HashMap<>();
            methodInfo.getNoteMap().forEach((key, value) -> {
                Object render = newAdapter.getProperty(value, Object.class);
                noteMap.put(key, render);
            });
            return new CatInterceptPoint(this);
        }

    }




}
