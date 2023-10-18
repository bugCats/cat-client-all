package cc.bugcat.catserver.utils;

import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.asm.CatInterfaceEnhancer;
import cc.bugcat.catserver.asm.CatServerInstance;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Multimaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import springfox.documentation.service.ResourceGroup;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import springfox.documentation.spi.service.contexts.RequestMappingContext;
import springfox.documentation.spring.web.ControllerNamingUtils;
import springfox.documentation.spring.web.scanners.ApiListingReferenceScanResult;
import springfox.documentation.spring.web.scanners.ApiListingReferenceScanner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    private final Logger logger = LoggerFactory.getLogger(CatSwaggerScanner.class);

    @Override
    public ApiListingReferenceScanResult scan(DocumentationContext context) {

        try {
            SwaggerAdapter swaggerAdapter = SwaggerAdapter.getAdapter(context.getClass().getClassLoader());

            ArrayListMultimap<ResourceGroup, RequestMappingContext> resourceGroupRequestMappings = ArrayListMultimap.create();

            Iterable<Object> iterable = (Iterable) FluentIterable.from(context.getRequestHandlers())
                    .filter(context.getApiSelector().getRequestHandlerSelector());

            Map<CatSwaggerEntry, List<Object>> requestGroup = StreamSupport.stream(iterable.spliterator(), false)
                    .collect(Collectors.groupingBy(handler -> new CatSwaggerEntry(swaggerAdapter.getHandlerMethod(handler))));

            for ( Map.Entry<CatSwaggerEntry, List<Object>> entry : requestGroup.entrySet() ) {

                CatSwaggerEntry swaggerEntry = entry.getKey();
                List handlers = entry.getValue();

                if( CatInterfaceEnhancer.isBridgeClass(swaggerEntry.ctrlClass) ){
                    // 是asm增强后Interface

                    // gclib创建的ctrl对象
                    CatServerInstance ctrlBean = (CatServerInstance) swaggerEntry.ctrlBean;
                    Class serverClass = ctrlBean.getServerProperty().getServerClass();
                    CatSwaggerHandler swaggerHandler = swaggerEntry.parseMethodFrom(serverClass);

                    for (Object handler : handlers) {

                        RequestMappingInfo requestMappingInfo = swaggerAdapter.getRequestMapping(handler);
                        HandlerMethod handlerMethod = swaggerAdapter.getHandlerMethod(handler);

                        Method method = handlerMethod.getMethod();

                        // 原feign-interface
                        Class realInterClass = swaggerHandler.getMethodFrom(method);

                        // asm增强后的Interface
                        Class ctrlInterClass = swaggerHandler.getCtrlInterface(realInterClass);
                        if( ctrlInterClass == null ){
                            ctrlInterClass = realInterClass;
                        }

                        ResourceGroup resourceGroup = new ResourceGroup(controllerNameAsGroup(realInterClass), realInterClass, 0);

                        CatHandlerMethod catHandlerMethod = new CatHandlerMethod(ctrlInterClass, handlerMethod);

                        RequestMappingContext requestMappingContext = swaggerAdapter.createRequestMappingContext(context, handler, requestMappingInfo, catHandlerMethod);
                        resourceGroupRequestMappings.put(resourceGroup, requestMappingContext);
                    }

                } else {
                    // 普通controller
                    for (Object handler : handlers) {
                        RequestMappingInfo requestMappingInfo = swaggerAdapter.getRequestMapping(handler);
                        HandlerMethod handlerMethod = swaggerAdapter.getHandlerMethod(handler);
                        ResourceGroup resourceGroup = new ResourceGroup(ControllerNamingUtils.controllerNameAsGroup(handlerMethod), handlerMethod.getBeanType(), 0);
                        RequestMappingContext requestMappingContext = swaggerAdapter.createRequestMappingContext(context, handler, requestMappingInfo, handlerMethod);
                        resourceGroupRequestMappings.put(resourceGroup, requestMappingContext);
                    }
                }
            }

            return new ApiListingReferenceScanResult(Multimaps.asMap(resourceGroupRequestMappings));

        } catch ( Exception ex ) {
            logger.error("CatSwaggerScanner 执行失败：{}；使用默认模式！" + ex.getMessage());
            return super.scan(context);
        }
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
        private final Map<String, Class> methodMap = new HashMap<>();

        // ctrl对象的Interface
        private final Map<String, Class> ctrlMap = new HashMap<>();

        // ctrl对象的Interface继承关系：父类：子类集合
        private final Map<Class, List<Class>> ifaceTree = new HashMap<>();

        /**
         * @param serverClass 原始的被@CatServer注解类的class
         * */
        private void parseClassFrom(Class ctrlClass, Class serverClass){
            Set<Class> ifaceSet = new HashSet<>();
            getInterfaces(serverClass, ifaceSet, ifaceTree);
            for( Class inter : ifaceSet ){
                boolean isCatface = inter.getAnnotation(Catface.class) != null; //如果是精简模式，直接存方法名
                for ( Method im : inter.getMethods() ) {
                    String sign = isCatface ? im.getName() : CatToosUtil.signature(im);
                    methodMap.put(sign, im.getDeclaringClass());
                }
            }

            for ( Class ctrlInter : ctrlClass.getInterfaces() ) { //ctrl类的直接接口，如果接口实现了继承，还需要再解析
                if( CatInterfaceEnhancer.isBridgeClass(ctrlInter) ){
                    ctrlMap.put(ctrlInter.getSimpleName(), ctrlInter);
                }
            }
        }

        private static void getInterfaces(Class clazz, Set<Class> ifaceSet, Map<Class, List<Class>> ifaceTree){
            if( clazz == null ){
                return;
            }
            if( clazz.isInterface() ){
                ifaceSet.add(clazz);
                Class[] interfaces = clazz.getInterfaces();
                for(Class inter : interfaces){
                    List<Class> list = ifaceTree.get(inter);
                    if( list == null ){
                        list = new ArrayList<>();
                        ifaceTree.put(inter, list);
                    }
                    list.add(clazz);
                    getInterfaces(inter, ifaceSet, ifaceTree);
                }
            } else {
                Class superClass = clazz;
                while ( superClass != null && superClass != Object.class ) {
                    Class[] interfaces = superClass.getInterfaces();
                    for ( Class sinter : interfaces ) {
                        getInterfaces(sinter, ifaceSet, ifaceTree);
                    }
                    superClass = superClass.getSuperclass();
                }
            }
        }


        /**
         * 返回该方法属于哪个feign-interface
         * */
        private Class getMethodFrom(Method method){
            Class inter = methodMap.getOrDefault(CatToosUtil.signature(method), methodMap.get(method.getName()));
            return inter;
        }

        /**
         * 通过feign-interface获取增强后的Interface
         * */
        private Class getCtrlInterface(Class inter){
            Class bridgeClass = ctrlMap.get(CatInterfaceEnhancer.bridgeClassSimpleName(inter));
            if( bridgeClass == null ){
                List<Class> list = ifaceTree.get(inter);
                for ( Class iface : list ) {
                    bridgeClass = getCtrlInterface(iface);
                    if( bridgeClass != null ){
                        return bridgeClass;
                    }
                }
            }
            return bridgeClass;
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



    private static class ServerInterfaceTree {
        private final Class serverInterface;
        private final Class[] interfaces;
        public ServerInterfaceTree(Class serverInterface, Class[] interfaces) {
            this.serverInterface = serverInterface;
            this.interfaces = interfaces;
        }

        @Override
        public boolean equals(Object other) {
            if ( this == other )
                return true;
            if ( other == null || getClass() != other.getClass() )
                return false;
            ServerInterfaceTree that = (ServerInterfaceTree) other;
            return serverInterface.equals(that.serverInterface);
        }
        @Override
        public int hashCode() {
            return Objects.hash(serverInterface);
        }
    }

}
