package cc.bugcat.catserver.handler;

import cc.bugcat.catserver.beanInfos.CatServerInfo;
import org.springframework.core.type.StandardMethodMetadata;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * 拦截器 方法之间的入参
 * 对于每次请求，为单例
 * */
public final class CatInterceptPoint {


    private final Map<Class, Map<String, Object>> annAttrMap = new HashMap<>(); // 注解属性
    private final Map<String, Object> attributesMap = new HashMap<>();    //自定义属性


    private final HttpServletRequest request;
    private final HttpServletResponse response;

    private final CatServerInfo serverInfo;
    private final Method method;            //访问的方法
    private final Object target;            //访问的对象
    private final Object[] arguments;       //方法入参
    private final StandardMethodMetadata interMethod;    //对应的interface的方法


    private CatInterceptPoint(Builder builder) {
        this.request = builder.request;
        this.response = builder.response;
        this.serverInfo = builder.serverInfo;
        this.target = builder.target;
        this.interMethod = builder.interMethod;
        this.method = interMethod.getIntrospectedMethod();
        this.arguments = builder.arguments;
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
            Map<String, Object> map = interMethod.getAnnotationAttributes(annotationClass.getName());
            if( map == null ){
                map = new HashMap<>();
            }
            attr = Collections.unmodifiableMap(map);
            annAttrMap.put(annotationClass, attr);
        }
        return attr;
    }

    public CatInterceptPoint putAttribute(String name, Object value){
        attributesMap.put(name, value);
        return this;
    }
    public <T> T getAttribute(String name, Class<T> clazz){
        return (T) attributesMap.get(name);
    }
    public <T> T getAttribute(String name, T defaultValue){
        return (T) attributesMap.getOrDefault(name, defaultValue);
    }
    public Map<String, Object> getAttributesMap() {
        return attributesMap;
    }

    public HttpServletRequest getRequest() {
        return request;
    }
    public HttpServletResponse getResponse() {
        return response;
    }
    public Class<?> getServerClass(){
        return serverInfo.getServerClass();
    }
    public Object getTarget() {
        return target;
    }
    public Method getMethod() {
        return method;
    }
    public Object[] getArguments() {
        return arguments;
    }



    protected static Builder builder(){
        return new Builder();
    }


    protected static class Builder {
        private HttpServletRequest request;
        private HttpServletResponse response;
        private CatServerInfo serverInfo;
        private Object target;
        private StandardMethodMetadata interMethod;
        private Object[] arguments;


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

        public Builder target(Object target) {
            this.target = target;
            return this;
        }

        public Builder interMethod(StandardMethodMetadata interMethod) {
            this.interMethod = interMethod;
            return this;
        }

        public Builder arguments(Object[] arguments) {
            this.arguments = arguments;
            return this;
        }

        public CatInterceptPoint build(){
            return new CatInterceptPoint(this);
        }

    }




}
