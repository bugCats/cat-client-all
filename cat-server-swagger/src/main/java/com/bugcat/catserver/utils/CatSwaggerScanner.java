package com.bugcat.catserver.utils;

import com.bugcat.catserver.annotation.CatServer;
import com.google.common.collect.ArrayListMultimap;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import springfox.documentation.RequestHandler;
import springfox.documentation.service.ResourceGroup;
import springfox.documentation.spi.service.contexts.ApiSelector;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import springfox.documentation.spi.service.contexts.RequestMappingContext;
import springfox.documentation.spring.web.scanners.ApiListingReferenceScanResult;
import springfox.documentation.spring.web.scanners.ApiListingReferenceScanner;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Multimaps.asMap;
import static springfox.documentation.spring.web.ControllerNamingUtils.controllerNameAsGroup;
import static springfox.documentation.spring.web.paths.Paths.splitCamelCase;

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
            ResourceGroup resourceGroup = new ResourceGroup(controllerNameAsGroup(handlerMethod), handlerMethod.getBeanType(), 0);

            // 通过原始RequestHandler，生成增强后的CatHandlerMethod
            handlerMethod = new CatHandlerMethod(handlerMethod);
            
            RequestMappingContext requestMappingContext = new RequestMappingContext(context, requestMappingInfo, handlerMethod);
            resourceGroupRequestMappings.put(resourceGroup, requestMappingContext);
        }
        
        return new ApiListingReferenceScanResult(asMap(resourceGroupRequestMappings));
    }
    
    
    private static class CatHandlerMethod extends HandlerMethod {

        private Class beanType;
        
        protected CatHandlerMethod(HandlerMethod handlerMethod) {
            super(handlerMethod);
        }

        @Override
        public Class<?> getBeanType() {
            if( beanType == null ){
                beanType = super.getBeanType();
                if( AnnotationUtils.findAnnotation(beanType, CatServer.class) != null ){ //如果包含CatServer注解，直接返回当前对象的class
                    beanType = super.getBean().getClass();
                }
            } 
            return beanType;
        }
        
    }


//    public static String controllerNameAsGroup(HandlerMethod handlerMethod) {
//        Class<?> controllerClass = ClassUtils.getUserClass(handlerMethod.getBeanType());
//        return splitCamelCase(controllerClass.getSimpleName(), "-")
//                .replace("/", "")
//                .toLowerCase();
//    }
}
