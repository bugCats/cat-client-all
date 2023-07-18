package cc.bugcat.catserver.utils;

import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastConstructor;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import springfox.documentation.spi.service.contexts.RequestMappingContext;

import java.util.HashMap;
import java.util.Map;


/**
 * springfox 兼容2.6版本前后
 * */
class SwaggerAdapter {

    static SwaggerAdapter getAdapter(ClassLoader classLoader){
        SwaggerAdapter adapter = null;
        try {

            final String requestHandlerName = "springfox.documentation.RequestHandler";
            final String requestMappingContextName = "springfox.documentation.spi.service.contexts.RequestMappingContext";
            
            Class<?> requestHandlerClass = classLoader.loadClass(requestHandlerName);
            Class<?> requestMappingContextClass = classLoader.loadClass(requestMappingContextName);
            FastClass requestHandlerFastClass = FastClass.create(requestHandlerClass);
            FastClass requestMappingContextFastClass = FastClass.create(requestMappingContextClass);

            adapter = new SwaggerAdapter();
            adapter.handlerMethod = requestHandlerFastClass.getMethod("getHandlerMethod", new Class[0]);
            adapter.requestMapping = requestHandlerFastClass.getMethod("getRequestMapping", new Class[0]);

            if( requestHandlerClass.isInterface() ){ // swagger 2.6 开始，RequestHandler修改成为了interface
                Class[] argsType = new Class[]{DocumentationContext.class, requestHandlerClass};
                FastConstructor constructor = requestMappingContextFastClass.getConstructor(argsType);
                adapter.argsName = new String[]{"DocumentationContext", "RequestHandler"};
                adapter.constructor = constructor;
            } else {
                Class[] argsType = new Class[]{DocumentationContext.class, RequestMappingInfo.class, HandlerMethod.class};
                FastConstructor constructor = requestMappingContextFastClass.getConstructor(argsType);
                adapter.argsName = new String[]{"DocumentationContext", "RequestMappingInfo", "HandlerMethod"};
                adapter.constructor = constructor;
            }
            
        } catch ( Exception ex) {
            ex.printStackTrace();
        }
        return adapter;
    }


    
    private FastMethod handlerMethod;
    private FastMethod requestMapping;
    private String[] argsName;
    private FastConstructor constructor;

    RequestMappingContext createRequestMappingContext(DocumentationContext context, Object handler, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("DocumentationContext", context);
        argsMap.put("RequestHandler", handler);
        argsMap.put("RequestMappingInfo", requestMappingInfo);
        argsMap.put("HandlerMethod", handlerMethod);
        Object[] args = new Object[argsName.length];
        for ( int idx = 0; idx < argsName.length; idx++ ) {
            args[idx] = argsMap.get(argsName[idx]);
        }
        try {
            RequestMappingContext result = (RequestMappingContext) constructor.newInstance(args);
            return result;
        } catch ( Exception ex ) {
            return null;
        }
    }

    
    HandlerMethod getHandlerMethod(Object requestHandler){
        try {
            HandlerMethod value = (HandlerMethod) handlerMethod.invoke(requestHandler, null);
            return value;
        } catch ( Exception ex ) {
            throw new RuntimeException(new NoSuchMethodException("getHandlerMethod"));
        }
    }
    RequestMappingInfo getRequestMapping(Object requestHandler){
        try {
            RequestMappingInfo value = (RequestMappingInfo) requestMapping.invoke(requestHandler, null);
            return value;
        } catch ( Exception ex ) {
            throw new RuntimeException(new NoSuchMethodException("getRequestMapping"));
        }
    }
    


}
