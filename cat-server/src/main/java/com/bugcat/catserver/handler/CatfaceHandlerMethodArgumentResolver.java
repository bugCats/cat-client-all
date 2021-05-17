package com.bugcat.catserver.handler;

import com.bugcat.catface.annotation.Catface;
import com.bugcat.catface.utils.CatToosUtil;
import org.springframework.core.Conventions;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;

/**
 * 由于自定义参数解析器，加载晚于系统
 * 部分参数类型无法解析到：Map
 * 只能通过拦截器中，继续处理
 * */
public class CatfaceHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver{
    
    
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasMethodAnnotation(Catface.class);
    }

    
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        CatArgumentResolver resolver = CatArgumentResolver.build(parameter.getMethod());
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Object requestBody = webRequest.getAttribute(CatToosUtil.bridgeName, RequestAttributes.SCOPE_REQUEST);
        if( requestBody == null ){
            requestBody = resolver.readRequestBody(servletRequest);
            webRequest.setAttribute(CatToosUtil.bridgeName, requestBody, RequestAttributes.SCOPE_REQUEST);
        }
        Object argument = resolver.resolveNameArgument(parameter.getParameterIndex(), requestBody);
        if (argument != null) {
            String name = Conventions.getVariableNameForParameter(parameter);
            WebDataBinder binder = binderFactory.createBinder(webRequest, argument, name);
            validateIfApplicable(binder, parameter);
            if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
                throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
            }
        }
        return argument;
    }

    
    private void validateIfApplicable(WebDataBinder binder, MethodParameter parameter) {
        Annotation[] annotations = parameter.getParameterAnnotations();
        for (Annotation ann : annotations) {
            Validated validatedAnn = AnnotationUtils.getAnnotation(ann, Validated.class);
            if (validatedAnn != null || ann.annotationType().getSimpleName().startsWith("Valid")) {
                Object hints = (validatedAnn != null ? validatedAnn.value() : AnnotationUtils.getValue(ann));
                Object[] validationHints = (hints instanceof Object[] ? (Object[]) hints : new Object[] {hints});
                binder.validate(validationHints);
                break;
            }
        }
    }
    
    private boolean isBindExceptionRequired(WebDataBinder binder, MethodParameter parameter) {
        int i = parameter.getParameterIndex();
        Class<?>[] paramTypes = parameter.getMethod().getParameterTypes();
        boolean hasBindingResult = (paramTypes.length > (i + 1) && Errors.class.isAssignableFrom(paramTypes[i + 1]));
        return !hasBindingResult;
    }
    
}
