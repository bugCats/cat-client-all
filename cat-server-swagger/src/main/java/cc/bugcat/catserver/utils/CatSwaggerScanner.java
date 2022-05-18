package cc.bugcat.catserver.utils;

import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.asm.CatInterfaceEnhancer;
import cc.bugcat.catserver.asm.CatServerHandler;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Multimaps;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import springfox.documentation.RequestHandler;
import springfox.documentation.service.ResourceGroup;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import springfox.documentation.spi.service.contexts.RequestMappingContext;
import springfox.documentation.spring.web.ControllerNamingUtils;
import springfox.documentation.spring.web.scanners.ApiListingReferenceScanResult;
import springfox.documentation.spring.web.scanners.ApiListingReferenceScanner;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static springfox.documentation.spring.web.paths.Paths.splitCamelCase;


/**
 * 
 * swagger扫描
 * 如果启用swagger，需要修改扫描地方
 *
 * swagger 2.5.0版本
 * 
 * @author bugcat
 * */
public class CatSwaggerScanner extends ApiListingReferenceScanner {

    @Override
    public ApiListingReferenceScanResult scan(DocumentationContext context) {
        
        ArrayListMultimap<ResourceGroup, RequestMappingContext> resourceGroupRequestMappings = ArrayListMultimap.create();

        Iterable<RequestHandler> iterable = FluentIterable.from(context.getRequestHandlers())
                .filter(context.getApiSelector().getRequestHandlerSelector());

        Map<CatSwaggerEntry, List<RequestHandler>> requestGroup = StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.groupingBy(handler -> new CatSwaggerEntry(handler.getHandlerMethod())));

        for ( Map.Entry<CatSwaggerEntry, List<RequestHandler>> entry : requestGroup.entrySet() ) {

            CatSwaggerEntry swaggerEntry = entry.getKey();
            List<RequestHandler> handlers = entry.getValue();

            if( CatInterfaceEnhancer.isBridgeClass(swaggerEntry.ctrlClass) ){
                // 是asm增强后Interface
                
                // gclib创建的ctrl对象
                CatServerHandler ctrlBean = (CatServerHandler) swaggerEntry.ctrlBean;
                Class serverClass = ctrlBean.getCatServerClass();
                CatSwaggerHandler swaggerHandler = swaggerEntry.parseMethodFrom(serverClass);
                
                for (RequestHandler handler : handlers) {

                    RequestMappingInfo requestMappingInfo = handler.getRequestMapping();
                    HandlerMethod handlerMethod = handler.getHandlerMethod();
                    Method method = handlerMethod.getMethod();

                    // 原feign-interface
                    Class realInterClass = swaggerHandler.getMethodFrom(method);
                    
                    // asm增强后的Interface
                    Class ctrlInterClass = swaggerHandler.getCtrlInterface();

                    ResourceGroup resourceGroup = new ResourceGroup(controllerNameAsGroup(realInterClass), realInterClass, 0);

                    CatHandlerMethod catHandlerMethod = new CatHandlerMethod(ctrlInterClass, handlerMethod);

                    RequestMappingContext requestMappingContext = new RequestMappingContext(context, requestMappingInfo, catHandlerMethod);
                    resourceGroupRequestMappings.put(resourceGroup, requestMappingContext);

                }
                
            } else {
                // 普通controller
                
                for (RequestHandler handler : handlers) {
                    RequestMappingInfo requestMappingInfo = handler.getRequestMapping();
                    HandlerMethod handlerMethod = handler.getHandlerMethod();
                    ResourceGroup resourceGroup = new ResourceGroup(ControllerNamingUtils.controllerNameAsGroup(handlerMethod), handlerMethod.getBeanType(), 0);
                    RequestMappingContext requestMappingContext = new RequestMappingContext(context, requestMappingInfo, handlerMethod);
                    resourceGroupRequestMappings.put(resourceGroup, requestMappingContext);
                }
            }
        }

        return new ApiListingReferenceScanResult(Multimaps.asMap(resourceGroupRequestMappings));
    }


    
    public static String controllerNameAsGroup(Class controllerClass) {
        return splitCamelCase(controllerClass.getSimpleName(), "-")
                .replace("/", "")
                .toLowerCase();
    }


    private static class CatSwaggerEntry {
        
        private final Class ctrlClass;
        private final Object ctrlBean;

        private CatSwaggerEntry(HandlerMethod handler) {
            this.ctrlClass = handler.getBeanType();
            this.ctrlBean = handler.getBean();
        }

        @Override
        public boolean equals(Object other) {
            if ( this == other ) {
                return true;
            }
            if ( getClass() != other.getClass() ) {
                return false;
            }
            CatSwaggerEntry that = (CatSwaggerEntry) other;
            return ctrlClass.equals(that.ctrlClass);
        }

        @Override
        public int hashCode() {
            return ctrlClass.hashCode();
        }

        private CatSwaggerHandler parseMethodFrom(Class serverClass){
            CatSwaggerHandler swaggerHandler = new CatSwaggerHandler();
            swaggerHandler.parseClassFrom(ctrlBean.getClass(), serverClass);
            return swaggerHandler;
        }
    }


    private static class CatSwaggerHandler {

        // 方法签名：feign-interface
        private Map<String, Class> infoMap;
        
        // ctrl对象的Interface
        private Class ctrlInterface; 
        
        /**
         * @param serverClass 原始的被@CatServer注解类的class
         * */
        private void parseClassFrom(Class ctrlClass, Class serverClass){
            this.infoMap = new HashMap<>();
            Stack<Class> serverInters = new Stack<>();
            getInterfaces(serverClass, serverInters);
            for( Class inter : serverInters ){
                boolean isCatface = inter.getAnnotation(Catface.class) != null; //如果是精简模式，直接存方法名
                for ( Method im : inter.getMethods() ) {
                    String sign = isCatface ? im.getName() : CatToosUtil.signature(im);
                    infoMap.put(sign, im.getDeclaringClass());
                }
            }

            for ( Class ctrlInter : ctrlClass.getInterfaces() ) {
                if( ctrlInter.getSimpleName().contains(CatServerUtil.BRIDGE_NAME) ){
                    this.ctrlInterface = ctrlInter;
                    break;
                }
            }
        }

        private static void getInterfaces(Class clazz, Stack<Class> inters){
            if( clazz == null ){
                return;
            }
            if( clazz.isInterface() ){
                Class[] interfaces = clazz.getInterfaces();
                if( interfaces.length == 0 ){
                    inters.add(clazz);
                    return;
                }
                for(Class inter : interfaces){
                    getInterfaces(inter, inters);
                }
                inters.add(clazz);
            } else {
                Class superClass = clazz;
                while ( superClass != null && superClass != Object.class ) {
                    Class[] interfaces = superClass.getInterfaces();
                    for ( Class sinter : interfaces ) {
                        getInterfaces(sinter, inters);
                    }
                    superClass = superClass.getSuperclass();
                }
            }
        }

        
        /**
         * 返回该方法属于哪个feign-interface
         * */
        private Class getMethodFrom(Method method){
            Class inter = infoMap.getOrDefault(CatToosUtil.signature(method), infoMap.get(method.getName()));
            return inter;
        }

        /**
         * 通过feign-interface获取增强后的Interface
         * */
        private Class getCtrlInterface(){
            return ctrlInterface;
        }

    }



    private static class CatHandlerMethod extends HandlerMethod {

        private final Class beanType;

        protected CatHandlerMethod(Class beanType, HandlerMethod handlerMethod) {
            super(handlerMethod);
            this.beanType = beanType;
        }

        @Override
        public Class getBeanType() {
            return beanType;
        }

    }


}
