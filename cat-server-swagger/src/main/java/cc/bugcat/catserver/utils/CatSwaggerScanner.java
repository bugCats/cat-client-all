package cc.bugcat.catserver.utils;

import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.annotation.CatServer;
import cc.bugcat.catserver.asm.CatInterfaceEnhancer;
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

import static springfox.documentation.spring.web.paths.Paths.splitCamelCase;


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

            Class beanType = handlerMethod.getBeanType();
            if( CatInterfaceEnhancer.isBridgeClass(beanType) ){

                // gclib创建的ctrl对象
                Object ctrl = handlerMethod.getBean();

                Method ctrlInterMethod = getCtrlMethodFrom(ctrl.getClass(), handlerMethod.getMethod());

                // api文档分组
                Class interClass = ctrlInterMethod.getDeclaringClass();
                Class realInterClass = getRealInterfaceClass(interClass);

                ResourceGroup resourceGroup = new ResourceGroup(controllerNameAsGroup(realInterClass), realInterClass, 0);
                CatHandlerMethod catHandler = new CatHandlerMethod(ctrlInterMethod.getDeclaringClass(), handlerMethod);

                RequestMappingContext requestMappingContext = new RequestMappingContext(context, requestMappingInfo, catHandler);
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


    /**
     * 查找method属于哪个增强interface
     * @param ctrlClass cglib生成的ctrl对象
     * @param method    需要查找的method
     * */
    private Method getCtrlMethodFrom(Class ctrlClass, Method method) {
        Map<String, Method> infoMap = weakMap.get(ctrlClass);
        if( infoMap == null ){
            infoMap = new HashMap<>();
            weakMap.put(ctrlClass, infoMap);

            List<Class> inters = new ArrayList<>();
            Class superClass = ctrlClass;
            while ( superClass != Object.class ) {
                for ( Class inter : superClass.getInterfaces() ) {
                    if( CatInterfaceEnhancer.isBridgeClass(inter) ){
                        inters.add(inter);
                    }
                }
                superClass = superClass.getSuperclass();
            }
            for( Class inter : inters ){
                for ( Method im : inter.getMethods() ) {
                    infoMap.put(CatToosUtil.signature(im), im);
                }
            }
        }
        return infoMap.get(CatToosUtil.signature(method));
    }


    public static String controllerNameAsGroup(Class controllerClass) {
        return splitCamelCase(controllerClass.getSimpleName(), "-")
                .replace("/", "")
                .toLowerCase();
    }

    public static Class getRealInterfaceClass(Class interClass){
        try {
            return CatServerUtil.getClassLoader().loadClass(interClass.getName().replace(CatServerUtil.BRIDGE_NAME, ""));
        } catch ( Exception e ) {
            return interClass;
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
