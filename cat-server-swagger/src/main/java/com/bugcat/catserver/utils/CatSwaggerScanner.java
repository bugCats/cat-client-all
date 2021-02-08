package com.bugcat.catserver.utils;

import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.catserver.annotation.CatServer;
import com.bugcat.catserver.asm.CatAsm;
import com.google.common.collect.ArrayListMultimap;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import springfox.documentation.RequestHandler;
import springfox.documentation.service.ResourceGroup;
import springfox.documentation.spi.service.contexts.ApiSelector;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import springfox.documentation.spi.service.contexts.RequestMappingContext;
import springfox.documentation.spring.web.scanners.ApiListingReferenceScanResult;
import springfox.documentation.spring.web.scanners.ApiListingReferenceScanner;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Multimaps.asMap;
import static springfox.documentation.spring.web.ControllerNamingUtils.controllerNameAsGroup;

/**
 * swagger扫描
 * 如果启用swagger，需要修改扫描地方
 * */
public class CatSwaggerScanner extends ApiListingReferenceScanner {

    
    /**
     * 
     * at：swagger 2.5.0
     * 
     * @link springfox.documentation.spring.web.readers.operation.HandlerMethodResolver 
     * 
     * 在[resolvedMethod]这个方法，倒数第二行：
     * Iterable<ResolvedMethod> filtered = filter(newArrayList(typeWithMembers.getMemberMethods()), methodNamesAreSame(handlerMethod.getMethod()));
     * 
     * 调用[methodNamesAreSame]方法，从HandlerMethod中获取beanType作为实际class，比较class中存在的method，和HandlerMethod中的method是否为同一个
     * 
     * 
     * cat-server会先增强interface，为interface动态添加桥连方法，
     * 最后通过cglib生成动态代理类，动态代理类中有增强后的桥连方法
     * 
     * 但是，原始类中不存在桥连方法！
     * 
     * 
     * 因此[methodNamesAreSame]方法永远返回空
     * 
     * 
     * 需要重写HandlerMethod#getBeanType，直接返回cglib生成动态代理类
     * 
     * 
     * 此处的重写，只会影响swagger生成文档部分，不会影响到其他地方
     * 
     * 
     * 
     * 
     * */
    
    
    
    
    
    @Override
    public ApiListingReferenceScanResult scan(DocumentationContext context) {
        
        ArrayListMultimap<ResourceGroup, RequestMappingContext> resourceGroupRequestMappings = ArrayListMultimap.create();
        
        ApiSelector selector = context.getApiSelector();
        
        Iterable<RequestHandler> matchingHandlers = from(context.getRequestHandlers()).filter(selector.getRequestHandlerSelector());

        for (RequestHandler handler : matchingHandlers) {
            
            RequestMappingInfo requestMappingInfo = handler.getRequestMapping();

            HandlerMethod handlerMethod = handler.getHandlerMethod();
//            

            Class<?> beanType = handlerMethod.getBeanType();
            if( AnnotationUtils.findAnnotation(beanType, CatServer.class) != null ){ //如果包含CatServer注解，直接返回当前对象的class

                HandlerClassMethod handlerClassMethod = HandlerClassMethod.handlerClassMethod(handlerMethod.getBean().getClass());
                Class methodFrom = handlerClassMethod.methodFrom(handlerMethod.getMethod());

                // 通过原始RequestHandler，生成增强后的CatHandlerMethod
                CatHandlerMethod method = new CatHandlerMethod(methodFrom.getInterfaces()[0], handlerMethod);
                ResourceGroup resourceGroup = new ResourceGroup(controllerNameAsGroup(method), method.getBeanType(), 0);

                method.setBeanType(methodFrom);
                RequestMappingContext requestMappingContext = new RequestMappingContext(context, requestMappingInfo, method);
                resourceGroupRequestMappings.put(resourceGroup, requestMappingContext); 
                        
            } else {
                ResourceGroup resourceGroup = new ResourceGroup(controllerNameAsGroup(handlerMethod), handlerMethod.getBeanType(), 0);
                RequestMappingContext requestMappingContext = new RequestMappingContext(context, requestMappingInfo, handlerMethod);
                resourceGroupRequestMappings.put(resourceGroup, requestMappingContext);
            }
        }
        
        return new ApiListingReferenceScanResult(asMap(resourceGroupRequestMappings));
    }
    
    
    private static class HandlerClassMethod {
    
        private static Map<Class, HandlerClassMethod> handlerClassMethodMap = new HashMap<>();
        
        /**
         * @param clazz cglib动态代理之后的对象class
         * */
        private static HandlerClassMethod handlerClassMethod(Class clazz){
            HandlerClassMethod handlerClassMethod = handlerClassMethodMap.get(clazz);
            if( handlerClassMethod == null ){
                handlerClassMethod = new HandlerClassMethod();
                handlerClassMethodMap.put(clazz, handlerClassMethod);
                Class[] inters = clazz.getInterfaces();
                for ( Class inter : inters ) {
                    if( CatAsm.isBridgeClass(inter) ){
                        for ( Method method : inter.getMethods() ) {
                            handlerClassMethod.methodClassMap.put(CatToosUtil.signature(method), inter);
                        }
                    }
                }
            }
            return handlerClassMethod;
        }
        
        
        private Map<String, Class> methodClassMap = new HashMap<>();
        public Class methodFrom(Method method){
            return methodClassMap.get(CatToosUtil.signature(method));
        }
        
    }
    
    private static class CatHandlerMethod extends HandlerMethod {

        private Class beanType;
        
        protected CatHandlerMethod(Class beanType, HandlerMethod handlerMethod) {
            super(handlerMethod);
            this.beanType = beanType;
        }

        @Override
        public Class<?> getBeanType() {
            return beanType;
        }

        public void setBeanType(Class beanType) {
            this.beanType = beanType;
        }
    }


}
