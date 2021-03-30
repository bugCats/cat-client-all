package com.bugcat.catserver.utils;

import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.catserver.asm.CatAsm;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Multimaps;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import springfox.documentation.RequestHandler;
import springfox.documentation.service.ResourceGroup;
import springfox.documentation.spi.service.contexts.ApiSelector;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import springfox.documentation.spi.service.contexts.RequestMappingContext;
import springfox.documentation.spring.web.ControllerNamingUtils;
import springfox.documentation.spring.web.scanners.ApiListingReferenceScanResult;
import springfox.documentation.spring.web.scanners.ApiListingReferenceScanner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * swagger扫描
 * 如果启用swagger，需要修改扫描地方
 * */
public class CatSwaggerScanner extends ApiListingReferenceScanner {
    
       
    @Override
    public ApiListingReferenceScanResult scan(DocumentationContext context) {
        
        ArrayListMultimap<ResourceGroup, RequestMappingContext> resourceGroupRequestMappings = ArrayListMultimap.create();
        
        ApiSelector selector = context.getApiSelector();
        
        Iterable<RequestHandler> matchingHandlers = FluentIterable.from(context.getRequestHandlers()).filter(selector.getRequestHandlerSelector());

        for (RequestHandler handler : matchingHandlers) {
            
            RequestMappingInfo requestMappingInfo = handler.getRequestMapping();
            HandlerMethod handlerMethod = handler.getHandlerMethod();

            Class<?> beanType = handlerMethod.getBeanType();
            
            if( CatAsm.isBridgeClass(beanType) ){

                Class serverClass = CatServerUtil.getServerClass(beanType);
                Class methodFrom = getMethodFrom(serverClass, CatToosUtil.signature(handlerMethod.getMethod()));
                
                CatHandlerMethod method = new CatHandlerMethod(methodFrom, handlerMethod);
                ResourceGroup resourceGroup = new ResourceGroup(ControllerNamingUtils.controllerNameAsGroup(method), method.getBeanType(), 0);
                
                RequestMappingContext requestMappingContext = new RequestMappingContext(context, requestMappingInfo, method);
                resourceGroupRequestMappings.put(resourceGroup, requestMappingContext); 
                        
            } else {
                ResourceGroup resourceGroup = new ResourceGroup(ControllerNamingUtils.controllerNameAsGroup(handlerMethod), handlerMethod.getBeanType(), 0);
                RequestMappingContext requestMappingContext = new RequestMappingContext(context, requestMappingInfo, handlerMethod);
                resourceGroupRequestMappings.put(resourceGroup, requestMappingContext);
            }
        }
        
        return new ApiListingReferenceScanResult(Multimaps.asMap(resourceGroupRequestMappings));
    }


    private Map<Class, Map<String, Method>> weakMap = new WeakHashMap();
    private Class getMethodFrom(Class serverClass, String sign){
        Map<String, Method> infoMap = weakMap.get(serverClass);
        if( infoMap == null ){
            infoMap = new HashMap<>();
            weakMap.put(serverClass, infoMap);
            
            List<Class> inters = new ArrayList<>();
            Class superClass = serverClass;
            while ( superClass != Object.class ) {
                for ( Class inter : superClass.getInterfaces() ) {
                    inters.add(inter);
                }
                superClass = superClass.getSuperclass();
            }
            for( Class inter : inters ){
                for ( Method im : inter.getMethods() ) {
                    infoMap.put(CatToosUtil.signature(im), im);
                }
            }
        }
        Method method = infoMap.get(sign);
        return method != null ? method.getDeclaringClass() : serverClass;
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
    }


}
