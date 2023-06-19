package cc.bugcat.catserver.scanner;

import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.catface.handler.EnvironmentAdapter;
import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.asm.CatEnhancerDepend;
import cc.bugcat.catserver.handler.CatServerDepend;
import cc.bugcat.catserver.handler.CatServerInfo;
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
    private CatServerDepend serverDepend;

            
    @Override
    public void afterPropertiesSet() throws Exception {
        if( serverClassSet == null ){
            return;
        }

        CatEnhancerDepend enhancerDepend = new CatEnhancerDepend(serverClassSet.size());
        List<CatCtrlInfoBuilder> ctrlInfoBuilders = new ArrayList<>(serverClassSet.size());
        
        for(Class serverClass : serverClassSet){
            CatCtrlInfoBuilder builder = new CatCtrlInfoBuilder(serverClass, serverDepend, enhancerDepend);
            ctrlInfoBuilders.add(builder);
        }

        /**
         * level越大，说明继承次数越多，
         * 优先解析level小的对象，这样level大的对象，会覆盖level小的对象，保证继承性
         * */
        ctrlInfoBuilders.sort(CatCtrlInfoBuilder::compareTo);

        // 注册Controller
        registerController(ctrlInfoBuilders);

        enhancerDepend.clear();
        serverClassSet = null;
    }


    
    /**
     * 手动注册controller对象
     * */
    private void registerController(List<CatCtrlInfoBuilder> ctrlInfoBuilders) throws Exception {
        
        IntFunction<RequestMethod[]> requestMethodToArray = RequestMethod[]::new;
        IntFunction<String[]> stringToArray = String[]::new;
        RequestMappingHandlerMapping mapper = CatServerUtil.getBean(RequestMappingHandlerMapping.class);

        for( CatCtrlInfoBuilder builder : ctrlInfoBuilders ){
            CatCtrlInfo ctrlInfo = builder.build();

            CatServerInfo serverInfo = ctrlInfo.getServerInfo();
            
            Catface catface = serverInfo.getCatface();

            for(Method method : ctrlInfo.getBridgeMethods() ){

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
                mapper.registerMapping(mappingInfo, ctrlInfo.getController(), method); // 注册映射处理
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
