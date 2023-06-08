package cc.bugcat.catserver.scanner;

import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.catface.handler.EnvironmentAdapter;
import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.asm.CatEnhancerDepend;
import cc.bugcat.catserver.beanInfos.CatServerInfo;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.utils.CatServerUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.IntFunction;

/**
 * 将动态生成的interface实现类，注册成Controller
 * 
 * @author: bugcat
 * */
public class CatServerFactoryBean implements InitializingBean {

    /**
     * 包含有 @CatServer 注解的类
     * */
    private Set<Class> serverClassSet;

    /**
     * 全局配置项
     * */
    @Autowired
    private CatServerConfiguration serverConfig;

    /**
     * 环境变量
     * */
    @Autowired
    private ConfigurableListableBeanFactory configurableBeanFactory;
    
            
    @Override
    public void afterPropertiesSet() throws Exception {
        if( serverClassSet == null ){
            return;
        }

        EnvironmentAdapter envProp = EnvironmentAdapter.environmentProperty(configurableBeanFactory);
        CatEnhancerDepend enhancerDepend = new CatEnhancerDepend(serverConfig, envProp, serverClassSet.size());
        List<CatControllerFactory> controllerFactoryBeans = new ArrayList<>(serverClassSet.size());
        
        for(Class serverClass : serverClassSet){
            CatControllerFactory info = CatControllerFactory.builder()
                    .serverClass(serverClass)
                    .enhancerDepend(enhancerDepend)
                    .build();
            controllerFactoryBeans.add(info);
        }

        /**
         * level越大，说明继承次数越多，
         * 优先解析level小的对象，这样level大的对象，会覆盖level小的对象，保证继承性
         * */
        controllerFactoryBeans.sort(CatControllerFactory::compareTo);

        registerController(controllerFactoryBeans);

        serverClassSet = null;
    }


    
    /**
     * 手动注册controller对象
     * */
    private void registerController(List<CatControllerFactory> controllerFactoryBeans){
        
        IntFunction<RequestMethod[]> requestMethodToArray = RequestMethod[]::new;
        IntFunction<String[]> stringToArray = String[]::new;
        RequestMappingHandlerMapping mapper = CatServerUtil.getBean(RequestMappingHandlerMapping.class);

        for( CatControllerFactory factory : controllerFactoryBeans ){

            CatServerInfo serverInfo = factory.getServerInfo();
            Catface catface = serverInfo.getCatface();

            for(Method method : factory.getBridgeMethods() ){

                StandardMethodMetadata metadata = new StandardMethodMetadata(method);
                Map<String, Object> attributes = metadata.getAnnotationAttributes(CatServerUtil.REQUEST_MAPPING);

                if( serverInfo.isCatface() ){ //使用精简模式
                    attributes = new HashMap<>();
                    String serviceName = method.getDeclaringClass().getSimpleName().replace(CatServerUtil.BRIDGE_NAME, "");
                    attributes.put("value", new String[]{ CatToosUtil.getDefaultRequestUrl(catface, serviceName, method)});
                    attributes.put("method", new RequestMethod[]{RequestMethod.POST});
                }
                if( attributes == null ){
                    continue;
                }

                RequestMappingInfo mappingInfo = RequestMappingInfo
                        .paths(getValue(attributes, "value", stringToArray))
                        .methods(getValue(attributes, "method", requestMethodToArray))
                        .params(getValue(attributes, "params", stringToArray))
                        .headers(getValue(attributes, "headers", stringToArray))
                        .produces(getValue(attributes, "produces", stringToArray))
                        .consumes(getValue(attributes, "consumes", stringToArray))
                        .build();

                mapper.unregisterMapping(mappingInfo);
                mapper.registerMapping(mappingInfo, factory.getController(), method); // 注册映射处理
            }
        }
    }
    
    

    private final <T> T[] getValue(Map<String, Object> map, String key, IntFunction<T[]> func){
        Object value = map.get(key);
        if( value == null ){
            return func.apply(0);
        }
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


    public Set<Class> getServerClassSet() {
        return serverClassSet;
    }
    public void setServerClassSet(Set<Class> serverClassSet) {
        this.serverClassSet = serverClassSet;
    }

}
