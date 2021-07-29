package cc.bugcat.example.scanner;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;

public class CatCtrlInitializingBean implements InitializingBean, ApplicationContextAware{

    private ApplicationContext context;


    // interface的class
    private final Set<Class> servers;

    
    public CatCtrlInitializingBean(Set<Class> servers) {
        this.servers = servers;
    }
    

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }


    @Override
    public void afterPropertiesSet() throws Exception {

        IntFunction<RequestMethod[]> requestMethodToArray = RequestMethod[]::new;
        IntFunction<String[]> stringToArray = String[]::new;
        RequestMappingHandlerMapping mapper = context.getBean(RequestMappingHandlerMapping.class);
        String annName = RequestMapping.class.getName();
        
        for(Class clazz : servers){
            try {
                // 根据interface获取到组件
                Object bean = context.getBean(clazz);
                Method[] methods = clazz.getMethods();
                for(Method method : methods ){
                    StandardMethodMetadata metadata = new StandardMethodMetadata(method);
                    Map<String, Object> attr = metadata.getAnnotationAttributes(annName);
                    if( attr == null ){
                        continue;
                    }
                    RequestMappingInfo mappingInfo = RequestMappingInfo
                            .paths(getValue(attr, "value", stringToArray))
                            .methods(getValue(attr, "method", requestMethodToArray))
                            .params(getValue(attr, "params", stringToArray))
                            .headers(getValue(attr, "headers", stringToArray))
                            .produces(getValue(attr, "produces", stringToArray))
                            .consumes(getValue(attr, "consumes", stringToArray))
                            .build();
                    mapper.unregisterMapping(mappingInfo);
                    mapper.registerMapping(mappingInfo, bean, method); // 注册映射处理
                }
            } catch ( BeansException e ) {
                e.printStackTrace();
            }
        }
    }


    private final <T> T[] getValue(Map<String, Object> map, String key, IntFunction<T[]> func){
        Object value = map.get(key);
        if( value instanceof List ){
            List<T> list = ((List<T>)value);
            return list.toArray(func.apply(list.size()));
        } else if(value.getClass().isArray()){
            return (T[]) value;
        } else {
            T[] arr = func.apply(1);
            arr[0] = (T) value;
            return arr;
        }
    }
    
}
