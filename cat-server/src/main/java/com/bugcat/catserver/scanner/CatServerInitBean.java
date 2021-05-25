package com.bugcat.catserver.scanner;

import com.bugcat.catface.annotation.Catface;
import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.catserver.asm.CatAsmResult;
import com.bugcat.catserver.asm.CatInterfaceEnhancer;
import com.bugcat.catserver.beanInfos.CatServerInfo;
import com.bugcat.catserver.handler.CatFaceResolverBuilder;
import com.bugcat.catserver.handler.CatMethodInterceptorBuilder;
import com.bugcat.catserver.handler.CatMethodMapping;
import com.bugcat.catserver.utils.CatServerUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cglib.proxy.CallbackHelper;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
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

    
    private final static MethodInterceptor defaults = new MethodInterceptor() {
        @Override
        public Object intercept (Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            return methodProxy.invokeSuper(target, args);
        }
    };
    
    
    
    private final Set<Class> servers;
    private Map<Class, CatAsmResult> ctrlCacheMap;
    

    public CatServerInitBean(Set<Class> servers){
        this.servers = servers;
        this.ctrlCacheMap = new HashMap<>(servers.size() * 2);
    }
    
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
            CatServerInfo serverInfo = factory.serverInfo;
            Catface catface = serverInfo.getCatface(); 
            
            for(Method method : factory.bridgeMethods ){
                StandardMethodMetadata metadata = new StandardMethodMetadata(method);
                Map<String, Object> attrs = metadata.getAnnotationAttributes(CatToosUtil.annName);
                if( serverInfo.isCatface() ){
                    attrs = new HashMap<>();
                    attrs.put("value", new String[]{ CatToosUtil.getDefaultRequestUrl(catface, method)});
                    attrs.put("method", new RequestMethod[]{RequestMethod.POST});
                } else {
                    if( attrs == null ){
                        continue;
                    }
                }
                RequestMappingInfo mappingInfo = RequestMappingInfo
                        .paths(getValue(attrs, "value", stringToArray))
                        .methods(getValue(attrs, "method", requestMethodToArray))
                        .params(getValue(attrs, "params", stringToArray))
                        .headers(getValue(attrs, "headers", stringToArray))
                        .produces(getValue(attrs, "produces", stringToArray))
                        .consumes(getValue(attrs, "consumes", stringToArray))
                        .build();
                mapper.unregisterMapping(mappingInfo);
                mapper.registerMapping(mappingInfo, factory.ctrl, method); // 注册映射处理
            }
        }

        ctrlCacheMap = null;
    }

    
    private final class CtrlFactory implements Comparable<CtrlFactory> {
        
        private final Class serverClass;

        private CatServerInfo serverInfo;
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
            
            serverInfo = CatServerInfo.buildServerInfo(serverClass);
            ctrl = createCatCtrl(serverClass, serverInfo);
            
            for (Class superClass = serverClass; superClass != Object.class; superClass = superClass.getSuperclass() ) {
                level = level + 1;
            }
            
            Class thisClazz = ctrl.getClass(); //cglib动态生成的class => interface的实现类
            Class[] inters = thisClazz.getInterfaces();

            for( Class inter : inters ){ //增强后的interface
                if ( CatInterfaceEnhancer.isBridgeClass(inter) ) {
                    for( Method method : inter.getMethods() ){
                        bridgeMethods.add(method);
                    }
                }
            }
        }
    }



    private Object createCatCtrl(Class serverClass, CatServerInfo serverInfo) throws Exception {

        ClassLoader classLoader = CatServerUtil.getClassLoader();

        //类加载器
        CatInterfaceEnhancer serverAsm = new CatInterfaceEnhancer();

        // 被@CatServer标记的类，包含的所有interface
        List<Class> inters = new ArrayList<>();
        for ( Class superClass = serverClass; superClass != Object.class; superClass = superClass.getSuperclass()) {
            for ( Class inter : superClass.getInterfaces() ) {
                inters.add(inter);
            }
        }

        Map<String, StandardMethodMetadata> metadataMap = new HashMap<>();

        CatMethodMapping mapping = new CatMethodMapping();
        Map<String, CatFaceResolverBuilder> resolverMap = new HashMap<>();
        
        Class[] thisInters = new Class[inters.size()];

        for(int i = 0; i < inters.size(); i ++ ){ // 遍历每个interface
            Class inter = inters.get(i);

            CatAsmResult ctrlCache = ctrlCacheMap.get(inter);
            if( ctrlCache == null ){
                ctrlCache = serverAsm.enhancer(inter, serverInfo); //使用asm增强interface
                ctrlCacheMap.put(inter, ctrlCache);
            }

            mapping.putAll(ctrlCache.getMapping());
            resolverMap.putAll(ctrlCache.getResolverMap());
            thisInters[i] = ctrlCache.getEnhancerClass();
            
            for(Method method : inter.getMethods()){
                if( CatToosUtil.isObjectMethod(CatToosUtil.signature(method)) ){
                    continue;
                }
                String uuid = CatMethodMapping.uuid(method);
                StandardMethodMetadata metadata = new StandardMethodMetadata(method);
                metadataMap.put(uuid, metadata);
            }
        }
        // 此时thisInters中，全部为增强后的扩展interface
        
        
        CatMethodInterceptorBuilder builder = CatMethodInterceptorBuilder.builder();
        builder.serverInfo(serverInfo).serverClass(serverClass);
        
        CallbackHelper helper = new CallbackHelper(Object.class, thisInters) {
            @Override
            protected Object getCallback (Method method) {
                String uuid = mapping.getInterfaceUuid(method);
                StandardMethodMetadata metadata = metadataMap.get(uuid); 
                if ( metadata != null ) {//原interface方法
                    CatFaceResolverBuilder resolver = resolverMap.get(uuid);
                    builder.interMethodMetadata(metadata).argumentResolver(resolver.build());
                    return builder.build();
                } else {
                    return defaults;
                }
            }
        };

        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setSuperclass(Object.class);
        enhancer.setInterfaces(thisInters);
        enhancer.setCallbackFilter(helper);
        enhancer.setCallbacks(helper.getCallbacks());

        Object ctrl = enhancer.create();
        
        return ctrl;
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

}
