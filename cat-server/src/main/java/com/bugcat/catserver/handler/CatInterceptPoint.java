package com.bugcat.catserver.handler;

import org.springframework.core.type.StandardMethodMetadata;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CatInterceptPoint {
    
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final Object target;    //访问的对象
    private final Method method;    //访问的方法
    private final List<StandardMethodMetadata> interMethods;    //对应的interface的方法
    private final Object[] args;

    private final Map<Class, Map<String, Object>> annAttrMap = new HashMap<>();
    
    private final Map<String, Object> attrMap = new HashMap<>();

    protected Object result;        // after    正常执行有值

    protected Exception exception;  // after    出现异常有值

    
    public CatInterceptPoint(HttpServletRequest request, HttpServletResponse response,
                             Object target, Method method, List<StandardMethodMetadata> interMethods, Object[] args) {
        this.request = request;
        this.response = response;
        this.target = target;
        this.method = method;
        this.interMethods = interMethods;
        this.args = args;
    }
    
    
    public Map<String, Object> getAnnotations(Class annotationClass){
        Map<String, Object> attr = annAttrMap.get(annotationClass);
        if( attr == null ){
            Map<String, Object> tmp = new HashMap<>();
            for( StandardMethodMetadata metadata : interMethods ){
                Map<String, Object> map = metadata.getAnnotationAttributes(annotationClass.getName());
                if( map != null ){
                    tmp.putAll(map);
                }
            }
            attr = Collections.unmodifiableMap(tmp);
            annAttrMap.put(annotationClass, attr);
        }
        return attr;
    }
    
    
    public CatInterceptPoint putAttr(String name, Object value){
        attrMap.put(name, value);
        return this;
    }
    
    public <T> T getAttr(String name, Class<T> clazz){
        return (T) attrMap.get(name);
    }
    
    public <T> T getAttr(String name, T defaultValue){
        return (T) attrMap.getOrDefault(name, defaultValue);
    }
    
    

    public Map<String, Object> getAttrMap() {
        return attrMap;
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
    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }
    public Object getResult() {
        return result;
    }

    public Exception getException() {
        return exception;
    }
    public void setException(Exception exception) {
        this.exception = exception;
    }

    
}
