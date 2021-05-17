package com.bugcat.catserver.handler;

import com.alibaba.fastjson.JSONObject;
import com.bugcat.catface.utils.CatToosUtil;
import org.springframework.cglib.beans.BeanGenerator;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.core.MethodParameter;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 如果是精简模式情况，需要从Request中读流方式，获取到入参
 * 入参为json字符串，再解析成入参对象
 * */
final class CatArgumentResolver {
    
    private static Map<Method, CatArgumentResolver> methodMap = new ConcurrentHashMap<>();
    


    public static CatArgumentResolver build(Method srcMethod) {
        return methodMap.computeIfAbsent(srcMethod, method -> new CatArgumentResolver(method));
    }

    private final Class clazz;
    private final ParameterInfo[] infos;

    
    private CatArgumentResolver(Method method) {
        Parameter[] params = method.getParameters();
        this.infos = new ParameterInfo[params.length];
        
        BeanGenerator generator = new BeanGenerator();
        for ( int idx = 0; idx < params.length; idx++ ) {
            MethodParameter parameter = new MethodParameter(method, idx);
            infos[idx] = new ParameterInfo(parameter);
            generator.addProperty("arg" + idx, parameter.getParameterType());
        }
        this.clazz = (Class)generator.createClass();    //构建一个虚拟入参对象
        
        FastClass fastClass = FastClass.create(clazz);
        for(int idx = 0; idx < infos.length; idx ++){
            infos[idx].setFastMethod(fastClass);
        }
    }
    
    protected Object readRequestBody(HttpServletRequest request) throws Exception {
        Object requestBody = null;
        String body = readString(request);
        if( CatToosUtil.isBlank(body) ){
            requestBody = clazz.newInstance();
        } else {
            requestBody = JSONObject.parseObject(body, clazz);
        }
        return requestBody;
    }
    
    protected Object resolveNameArgument(int idx, Object requestBody) throws Exception {
        ParameterInfo info = infos[idx];
        Object arg = info.getValue(requestBody);
        return arg;
    }

    
    /**
     * 解析 Map 类型入参
     * */
    protected void resolveNameArgument(HttpServletRequest request, Object[] args) throws Exception {
        Object requestBody = request.getAttribute(CatToosUtil.bridgeName);
        for(ParameterInfo info : infos){
            if( info.noHit ){
                args[info.idx] = resolveNameArgument(info.idx, requestBody); 
            }
        }
        request.removeAttribute(CatToosUtil.bridgeName);
    }

    private static class ParameterInfo {

        private final int idx;
        private final boolean noHit;
        private FastMethod fastMethod;
        
        public ParameterInfo(MethodParameter parameter) {
            this.idx = parameter.getParameterIndex();
            this.noHit = isNoHitType(parameter.getParameterType());
        }
        public void setFastMethod(FastClass fastClass){
            this.fastMethod = fastClass.getMethod("getArg" + idx, new Class[0]);
        }
        
        public Object getValue(Object obj) throws Exception{
            Object arg = fastMethod.invoke(obj, null);
            return arg;
        }
    }
    
    private static boolean isNoHitType(Class parameterType){
        return Map.class.isAssignableFrom(parameterType);
    }
    
    private static String readString(HttpServletRequest request) throws Exception {
        try ( BufferedInputStream bis = new BufferedInputStream(request.getInputStream());
              ByteArrayOutputStream buf = new ByteArrayOutputStream();) {
            int result = bis.read();
            while(result != -1) {
                buf.write((byte) result);
                result = bis.read();
            }
            return buf.toString();
        } catch ( Exception e ) {
            throw e;
        }
    }


}
