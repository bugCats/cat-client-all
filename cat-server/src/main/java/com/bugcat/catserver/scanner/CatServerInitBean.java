package com.bugcat.catserver.scanner;

import com.bugcat.catface.spi.ResponesWrapper;
import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.catserver.asm.CatAsm;
import com.bugcat.catserver.beanInfos.CatServerInfo;
import com.bugcat.catserver.handler.CatMethodInterceptor;
import com.bugcat.catserver.handler.CatServiceCtrlInterceptor;
import com.bugcat.catserver.utils.CatServerUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cglib.proxy.CallbackHelper;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;

/**
 * 将动态生成的interface实现类，注册成Controller
 * @author: bugcat
 * */
public class CatServerInitBean implements InitializingBean{


    private Set<Class> servers;
    
    
    @Override
    public void afterPropertiesSet() throws Exception {

        if( servers == null ){
            return;
        }
        
        List<CtrlFactory> factories = new ArrayList<>(servers.size());
        for(Class serverClass : servers){
            CtrlFactory info = new CtrlFactory(serverClass);
            info.parse();
            factories.add(info);
        }
        
        /**
         * level越大，说明继承次数越多，
         * 优先解析level小的对象，这样level大的对象，会覆盖level小的对象，保证继承性
         * */
        factories.sort(CtrlFactory::compareTo);
        
        IntFunction<RequestMethod[]> requestMethodToArray = RequestMethod[]::new;
        IntFunction<String[]> stringToArray = String[]::new;

        RequestMappingHandlerMapping mapper = CatServerUtil.getBean(RequestMappingHandlerMapping.class);
        for( CtrlFactory factory : factories ){
            for(Method method : factory.bridgeMethods ){
                StandardMethodMetadata metadata = new StandardMethodMetadata(method);
                Map<String, Object> attr = metadata.getAnnotationAttributes(CatServerUtil.annName);
                RequestMappingInfo mappingInfo = RequestMappingInfo
                        .paths(getValue(attr, "value", stringToArray))
                        .methods(getValue(attr, "method", requestMethodToArray))
                        .params(getValue(attr, "params", stringToArray))
                        .headers(getValue(attr, "headers", stringToArray))
                        .produces(getValue(attr, "produces", stringToArray))
                        .consumes(getValue(attr, "consumes", stringToArray))
                        .build();
                mapper.unregisterMapping(mappingInfo);
                mapper.registerMapping(mappingInfo, factory.ctrl, method); // 注册映射处理
            }
        }
    }

    
    
    private static class CtrlFactory implements Comparable<CtrlFactory> {
        
        private Class serverClass;
        private int level = 0;  //继承关系：如果是子类，那么level比父类大，排在后面
        private Object ctrl;
        private Set<Method> bridgeMethods = new HashSet<>();

        private CtrlFactory(Class serverClass) {
            this.serverClass = serverClass;
        }
        
        
        @Override
        public int compareTo(CtrlFactory info) {
            return level - info.level;
        }
        
        
        private void parse() throws Exception {
            
            CatServerInfo serverInfo = CatServerInfo.buildServerInfo(serverClass);
            ctrl = createCatCtrl(serverClass, serverInfo);
            
            for (Class superClass = serverClass; superClass != Object.class; superClass = superClass.getSuperclass() ) {
                level = level + 1;
            }
            
            Class thisClazz = ctrl.getClass(); //cglib动态生成的class => interface的实现类
            Class[] inters = thisClazz.getInterfaces();

            for( Class inter : inters ){ //增强后的interface
                if ( CatAsm.isBridgeClass(inter) ) {
                    for( Method method : inter.getMethods() ){
                        bridgeMethods.add(method);
                    }
                }
            }
        }
    }

    
    
    private final static Object createCatCtrl(Class serverClass, CatServerInfo catServerInfo) throws Exception {

        ClassLoader classLoader = CatServerUtil.getClassLoader();
        
        Class warp = null;  //@CatServer上设置的统一响应包装器类
        Class<? extends ResponesWrapper> wrapper = catServerInfo.getWrapper();
        if( wrapper != null ){
            ResponesWrapper responesWrapper = ResponesWrapper.getResponesWrapper(wrapper);
            warp = responesWrapper.getWrapperClass();
        }

        //类加载器
        CatAsm asm = new CatAsm(classLoader);

        // 被@CatServer标记的类，包含的所有interface
        List<Class> inters = new ArrayList<>();
        for ( Class superClass = serverClass; superClass != Object.class; superClass = superClass.getSuperclass()) {
            for ( Class inter : superClass.getInterfaces() ) {
                inters.add(inter);
            }
        }

        Map<String, StandardMethodMetadata> metadataMap = new HashMap<>();
        
        Class[] thisInters = new Class[inters.size() + 1];
        thisInters[inters.size()] = CatServiceCtrlInterceptor.getServerClass();
        for(int i = 0; i < inters.size(); i ++ ){ // 遍历每个interface
            Class inter = inters.get(i);
            Class enhancer = asm.enhancer(inter, warp); //使用asm增强interface
            thisInters[i] = enhancer;
            for(Method method : inter.getMethods()){
                StandardMethodMetadata metadata = new StandardMethodMetadata(method);
                Map<String, Object> attr = metadata.getAnnotationAttributes(CatServerUtil.annName);
                if( attr != null ){// 遍历每个interface的方法，筛选只有包含CatServerInitBean.annName注解的
                    metadataMap.put(CatToosUtil.signature(method), metadata);
                }
            }
        }
        // 此时thisInters中，全部为增强后的扩展interface
        
        
        MethodInterceptor handerInterceptor = CatServiceCtrlInterceptor.getInstance();
        MethodInterceptor defaults = CatServiceCtrlInterceptor.getDefault();
        
        CallbackHelper helper = new CallbackHelper(Object.class, thisInters) {
            @Override
            protected Object getCallback (Method method) {
                StandardMethodMetadata metadata = metadataMap.get(CatToosUtil.signature(method));
                if ( metadata != null ) {
                    return new CatMethodInterceptor(metadata, catServerInfo); // ;
                } else {
                    String methodName = method.getName();
                    if( methodName.startsWith(CatServerUtil.bridgeName) ){
                        return handerInterceptor;
                    } else {
                        return defaults; 
                    }
                }
            }
        };

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Object.class);
        enhancer.setInterfaces(thisInters);
        enhancer.setCallbackFilter(helper);
        enhancer.setCallbacks(helper.getCallbacks());

        Object ctrl = enhancer.create();

        CatServiceCtrlInterceptor.setServerClass(ctrl, serverClass);
        
        return ctrl;
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


    
    public Set<Class> getServers() {
        return servers;
    }
    public void setServers(Set<Class> servers) {
        this.servers = servers;
    }
}
