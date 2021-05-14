package com.bugcat.catserver.handler;

import com.alibaba.fastjson.JSONObject;
import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.catserver.beanInfos.CatServerInfo;
import com.bugcat.catserver.utils.CatServerUtil;
import org.springframework.cglib.beans.BeanGenerator;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 如果是精简模式情况，需要从Request中读流方式，获取到入参
 * 入参为json字符串，再解析成入参对象
 * */
abstract class CatArgumentResolver {
    
    
    /**
     * 每个CatMethodInterceptor初始化时执行
     * */
    public static CatArgumentResolver build(CatServerInfo serverInfo, Method method){
        if( serverInfo.isCatface() ){
            return new CatfaceArgumentResolver(method);
        } else {
            return new NomalArgumentResolver();
        }
    }


    
    private CatArgumentResolver(){}
    
    
    /**
     * 解析入参
     * */
    protected abstract Object[] resolveNameArgument(HttpServletRequest request, Object[] args) throws Exception;
    
    
    
    /**
     * 普通模式，直接返回 args
     * */
    private static class NomalArgumentResolver extends CatArgumentResolver {
        @Override
        protected Object[] resolveNameArgument(HttpServletRequest request, Object[] args) {
            return args;
        }
    }
    
    

    private static class CatfaceArgumentResolver extends CatArgumentResolver {
        
        private final Map<String, ParameterInfo> propertyMap;
        private final Class clazz;

        public CatfaceArgumentResolver(Method method) {
            Parameter[] params = method.getParameters();
            
            BeanGenerator generator = new BeanGenerator();
            this.propertyMap = new LinkedHashMap<>(params.length * 2);
            for ( int i = 0; i < params.length; i++ ) {
                ParameterInfo info = new ParameterInfo(method, i);
                propertyMap.put(info.pname, info);
                generator.addProperty(info.pname, info.parameterType);
            }
            this.clazz = (Class)generator.createClass();    //构建一个虚拟入参对象
            
            FastClass fastClass = FastClass.create(clazz);
            propertyMap.forEach((key, value) -> {
                value.fastMethod = fastClass.getMethod("get" + CatToosUtil.capitalize(key), new Class[0]);
            });
        }

        @Override
        protected Object[] resolveNameArgument(HttpServletRequest request, Object[] args) throws Exception {
            if( args == null || args.length == 0 ){
                return args;
            }
            String body = readString(request);
            if( CatToosUtil.isBlank(body) ){
                return args;
            }
            Object resp = JSONObject.parseObject(body, clazz);
            int idx = 0;
            for(Map.Entry<String, ParameterInfo> entry : propertyMap.entrySet() ){
                ParameterInfo info = entry.getValue();
                Object arg = info.fastMethod.invoke(resp, null);
                validateIfApplicable(info, arg);
                args[idx ++] = arg;
            }
            return args;
        }
    }
    
    /**
     * 验证
     * */
    private final static void validateIfApplicable(ParameterInfo info, Object value) throws MethodArgumentNotValidException{
        if( info.needValid ){
            BeanPropertyBindingResult result = new BeanPropertyBindingResult(value, info.pname);
            ValidationUtils.invokeValidator(ParameterInfo.validator, value, result, info.validationHints);
            if (result.hasErrors()) {
                throw new MethodArgumentNotValidException(info.parameter, result);
            }
        }
    }
    
    
    private static class ParameterInfo {

        private static Validator validator; //需要延时加载
        static {
            try {
                Class validClass = Class.forName("javax.validation.Validator");
                validator = getExisting(validClass);
            } catch ( Exception e ) {
                validator = null;
            }
        }
        
        
        private MethodParameter parameter;
        private String pname;
        private Class parameterType;
        private boolean needValid;
        private Object[] validationHints;
        private FastMethod fastMethod;

        public ParameterInfo(Method method, int idx) {
            this.parameter = new MethodParameter(method, idx);
            RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
            this.pname = requestParam.value();
            this.parameterType = parameter.getParameterType();
            if( validator != null ){
                Annotation[] annotations = parameter.getParameterAnnotations();
                for (Annotation ann : annotations) {
                    Validated validatedAnn = AnnotationUtils.getAnnotation(ann, Validated.class);
                    if (validatedAnn != null || ann.annotationType().getSimpleName().startsWith("Valid")) {
                        Object hints = (validatedAnn != null ? validatedAnn.value() : AnnotationUtils.getValue(ann));
                        this.validationHints = (hints instanceof Object[] ? (Object[]) hints : new Object[] {hints});
                        this.needValid = true;
                        break;
                    }
                }
            }
        }
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


    private static Validator getExisting(Class validClass) {
        Object validator = CatServerUtil.getBean(validClass);
        if( validator == null ){
            return null;
        }
        if (validator instanceof Validator) {
            return (Validator) validator;
        } else {
            try {
                Constructor<SpringValidatorAdapter> constructor = SpringValidatorAdapter.class.getConstructor(validClass);
                return constructor.newInstance(validator);
            } catch ( Exception e ) {
                return null;
            }
        }
    }

}
