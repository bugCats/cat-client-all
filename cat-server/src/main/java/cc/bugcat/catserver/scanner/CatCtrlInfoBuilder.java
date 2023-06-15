package cc.bugcat.catserver.scanner;

import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.annotation.CatServer;
import cc.bugcat.catserver.asm.CatAsmInterface;
import cc.bugcat.catserver.asm.CatAsmMethod;
import cc.bugcat.catserver.asm.CatEnhancerDepend;
import cc.bugcat.catserver.asm.CatInterfaceEnhancer;
import cc.bugcat.catserver.asm.CatServerInstance;
import cc.bugcat.catserver.asm.CatServerProperty;
import cc.bugcat.catserver.beanInfos.CatServerInfo;
import cc.bugcat.catserver.handler.CatMethodAopInterceptor;
import cc.bugcat.catserver.handler.CatMethodInfo;
import cc.bugcat.catserver.handler.CatMethodInfoBuilder;
import cc.bugcat.catserver.utils.CatServerUtil;
import org.springframework.cglib.core.DefaultNamingPolicy;
import org.springframework.cglib.core.Predicate;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.CallbackHelper;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * controller对象工厂。
 * 先增强interface，然后根据增强后的Interface使用cglib动态生成controller对象。
 *
 * @author bugcat
 * */
class CatCtrlInfoBuilder implements Comparable<CatCtrlInfoBuilder> {

    public static CatCtrlInfoBuilder builder(Class serverClass, CatEnhancerDepend enhancerDepend){
        return new CatCtrlInfoBuilder(serverClass, enhancerDepend);
    }


    /**
     * 从controller中获取被@CatServer注解的对象
     * {@link CatServerInstance#getServerProperty()}
     * */
    private final static String serverPropertyMethodName = CatServerProperty.serverPropertyMethodName();

    /**
     * 被@CatServer标记的原始的实现类
     * */
    private final Class serverClass;
    /**
     * 全局配置项与缓存
     * */
    private final CatEnhancerDepend enhancerDepend;
    /**
     * serverClass类的继承关系
     * 如果是子类，那么level比父类大，排在后面
     * */
    private final int level;

    
    
    /**
     * {@link CatServer}注解信息
     * */
    protected CatServerInfo serverInfo;
    
    /**
     * 通过动态代理生成的controller对象
     * */
    protected Object controller;

    /**
     * 增强后的interface方法
     * */
    protected Set<Method> bridgeMethods = new HashSet<>();
    



    private CatCtrlInfoBuilder(Class serverClass, CatEnhancerDepend enhancerDepend) {
        this.serverClass = serverClass;
        this.enhancerDepend = enhancerDepend;

        // 当前CatServer类的继承层数，继承层级越多，level越大
        int level = 0;
        for (Class superClass = serverClass; superClass != Object.class; superClass = superClass.getSuperclass() ) {
            level = level + 1;
        }
        this.level = level;
    }


    @Override
    public int compareTo(CatCtrlInfoBuilder info) {
        return level - info.level;
    }

    
    

    protected CatCtrlInfo build() throws Exception {
        
        // CatServer注解信息
        this.serverInfo = CatServerInfo.build(serverClass, enhancerDepend);
        
        // 1. 增强被实现的interface
        // 2. 通过增强Interface动态代理生成controller
        this.controller = createController();

        
        //cglib动态生成的class => Interface的实现类
        Class thisClazz = controller.getClass(); 
        Class[] interfaces = thisClazz.getInterfaces();
        
        for( Class interfaceClass : interfaces ){ //增强后的interface
            if ( CatInterfaceEnhancer.isBridgeClass(interfaceClass) ) { //判断是否为增强后的Interface
                for( Method method : interfaceClass.getMethods() ){ // 增强后的Method
                    bridgeMethods.add(method);
                }
            }
        }

        return new CatCtrlInfo(this);
    }
    
    
    /**
     * 通过serverClass、与@CatServer注解，动态生成controller对象
     * */
    private Object createController() throws Exception {

        //类加载器
        ClassLoader classLoader = CatServerUtil.getClassLoader();

        CatInterfaceEnhancer serverAsm = new CatInterfaceEnhancer();

        // 被@CatServer标记的类，包含的所有原始interface。
        Stack<Class> interfaces = new Stack<>();
        for ( Class superClass = serverClass; superClass != Object.class; superClass = superClass.getSuperclass()) {
            for ( Class interfaceClass : superClass.getInterfaces() ) {
                interfaces.add(interfaceClass);
            }
        }

        // 增强后的接口类 asm-interface
        int lengthPointer = 0;
        Class[] thisInters = new Class[interfaces.size() + 1];
        thisInters[lengthPointer ++ ] = CatServerInstance.class;

        
        // 缓存原始CatServer类的method签名，最后需要与增强后的Method对应
        final Map<String, Method> serverClassMethodMap = new HashMap<>(serverClass.getMethods().length * 2);
        for ( Method method : serverClass.getMethods() ) {
            String signatureId = CatServerUtil.typeSignatureId(method);
            serverClassMethodMap.put(signatureId, method);
        }

        
        // 原interface方法签名id => interface的原始方法
        final Map<String, StandardMethodMetadata> metadataMap = new HashMap<>();

        // 增强后的Interface方法签名id => CatMethodInfo
        final Map<String, CatAsmMethod> allMethodInfoMap = new HashMap<>();

        // interface与对应的方法拦截器
        final Map<Method, CatMethodAopInterceptor> interceptorMap = enhancerDepend.getInterceptorMap();

        for (Class interClass : interfaces ){ // 遍历CatServer类的每个interface
            CatAsmInterface asmInterface = enhancerDepend.getControllerDescriptor(interClass);
            if( asmInterface == null ){
                asmInterface = serverAsm.enhancer(interClass, serverInfo, enhancerDepend); //使用asm增强interface
                enhancerDepend.putControllerDescriptor(interClass, asmInterface);
            }
            allMethodInfoMap.putAll(asmInterface.getMethodInfoMap());
            thisInters[lengthPointer ++] = asmInterface.getEnhancerClass(); // 增强后的Interface

            for(Method method : interClass.getMethods()){

                // 是否为object默认方法
                if( CatToosUtil.isObjectMethod(CatToosUtil.signature(method)) ){ 
                    continue;
                }
                
                String signatureId = CatServerUtil.typeSignatureId(method);
                StandardMethodMetadata metadata = new StandardMethodMetadata(method);
                metadataMap.put(signatureId, metadata);
            }
        }
        // 此时thisInters中，全部为增强后的扩展interface

        //被CatServer标记的类
        final Object serverBean = CatServerUtil.getBean(serverClass);
        final CatServerProperty serverProperty = new CatServerProperty(serverClass, serverBean, serverInfo);
        final CatMethodInfoBuilder methodBuilder = CatMethodInfoBuilder.builder(serverProperty);

        CatCallbackHelper helper = new CatCallbackHelper();
        helper.setEnhancerDepend(enhancerDepend)
                .setServerProperty(serverProperty)
                .setMethodBuilder(methodBuilder)
                .setAllMethodInfoMap(allMethodInfoMap)
                .setServerClassMethodMap(serverClassMethodMap)
                .setMetadataMap(metadataMap);
        
        CatCallbackFilter filter = new CatCallbackFilter();
        filter.parse(helper, Object.class, thisInters);
        
//        CallbackHelper helper = new CallbackHelper(Object.class, thisInters) {
//            @Override
//            protected Object getCallback (Method method) { // method为增强后的方法 asm-method
//
//                String signatureId = CatServerUtil.typeSignatureId(method);
//                CatAsmMethod methodInfo = allMethodInfoMap.get(signatureId);
//                if ( methodInfo != null ){ //如果是asm-interface的方法
//                    StandardMethodMetadata metadata = metadataMap.get(methodInfo.getInterfaceSignatureId());
//                    //原始CatServer类的方法
//                    Method serverMethod = serverClassMethodMap.get(methodInfo.getInterfaceSignatureId());
//
//                    CatMethodAopInterceptor interceptor = interceptorMap.get(serverMethod);
//                    if( interceptor != null ){
//                        return interceptor;
//                    }
//
//                    CatMethodInfo methodDesc = methodBuilder.interMethod(metadata)
//                            .controllerMethod(method)
//                            .serverMethod(serverMethod)
//                            .parameterResolver(methodInfo.getParameterResolver())
//                            .build();
//
//                    interceptor = new CatMethodAopInterceptor(methodDesc);
//
//                    interceptorMap.put(serverMethod, interceptor);
//                    return interceptor;
//                    
//                } else if ( serverPropertyMethodName.equals(method.getName()) ){ // 返回CatServer类的class
//                    return new MethodInterceptor() {
//                        @Override
//                        public Object intercept(Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
//                            return serverProperty;
//                        }
//                    };
//                }
//                
//                // object默认方法
//                return enhancerDepend.getObjectMethodInterceptor();
//            }
//        };
        
        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setSuperclass(Object.class);
        enhancer.setInterfaces(thisInters);
        enhancer.setCallbackFilter(filter);
        enhancer.setCallbacks(filter.getCallbacks());
        enhancer.setNamingPolicy(new ControllerNamingPolicy(serverClass));
        
        Object controller = enhancer.create(); // 一定是CatServerInstance的子类
        return controller;
    }

    
    private static class CatCallbackHelper {
        
        private CatEnhancerDepend enhancerDepend;

        // interface与对应的方法拦截器
        private Map<Method, CatMethodAopInterceptor> interceptorMap;

        // 被CatServer标记的类一些自定义属性
        private CatServerProperty serverProperty;

        private CatMethodInfoBuilder methodBuilder;
        
        // 增强后的Interface方法签名id => CatMethodInfo
        private Map<String, CatAsmMethod> allMethodInfoMap;
        
        // 原interface方法签名id => interface的原始方法
        private Map<String, StandardMethodMetadata> metadataMap;
        
        // 缓存原始CatServer类的method签名，最后需要与增强后的Method对应
        private Map<String, Method> serverClassMethodMap;


        public CatCallbackHelper setEnhancerDepend(CatEnhancerDepend enhancerDepend) {
            this.enhancerDepend = enhancerDepend;
            this.interceptorMap = enhancerDepend.getInterceptorMap();
            return this;
        }
        public CatCallbackHelper setServerProperty(CatServerProperty serverProperty) {
            this.serverProperty = serverProperty;
            return this;
        }
        public CatCallbackHelper setMethodBuilder(CatMethodInfoBuilder methodBuilder) {
            this.methodBuilder = methodBuilder;
            return this;
        }
        public CatCallbackHelper setAllMethodInfoMap(Map<String, CatAsmMethod> allMethodInfoMap) {
            this.allMethodInfoMap = allMethodInfoMap;
            return this;
        }
        public CatCallbackHelper setMetadataMap(Map<String, StandardMethodMetadata> metadataMap) {
            this.metadataMap = metadataMap;
            return this;
        }
        public CatCallbackHelper setServerClassMethodMap(Map<String, Method> serverClassMethodMap) {
            this.serverClassMethodMap = serverClassMethodMap;
            return this;
        }
    
    }
    

    private static class CatCallbackFilter implements CallbackFilter {
        
        private Map<Method, Integer> methodIndexMap = new HashMap();
        private List<Callback> callbacks = new ArrayList();
        private List<Class> callbackTypes = new ArrayList();

        public void parse(CatCallbackHelper helper, Class superclass, Class[] interfaces) {
            
            List<Method> methods = new ArrayList();
            Enhancer.getMethods(superclass, interfaces, methods);
            Map<Callback, Integer> indexes = new HashMap();

            for(int index = 0, size = methods.size(); index < size; index ++ ) {
                Method method = methods.get(index); // method为增强后的方法 asm-method
                Callback callback = this.getCallback(helper, method);
                if (indexes.get(callback) == null) {
                    indexes.put(callback, index);
                }
                this.methodIndexMap.put(method, index);
                this.callbacks.add(callback);
                this.callbackTypes.add(callback.getClass());
            }
        }

        private Callback getCallback (CatCallbackHelper helper, Method method) { 
            
            String signatureId = CatServerUtil.typeSignatureId(method);
            CatAsmMethod methodInfo = helper.allMethodInfoMap.get(signatureId);
            if ( methodInfo != null ){ //如果是asm-interface的方法
                StandardMethodMetadata metadata = helper.metadataMap.get(methodInfo.getInterfaceSignatureId());
                //原始CatServer类的方法
                Method serverMethod = helper.serverClassMethodMap.get(methodInfo.getInterfaceSignatureId());

                CatMethodAopInterceptor interceptor = helper.interceptorMap.get(serverMethod);
                if( interceptor != null ){
                    return interceptor;
                }

                CatMethodInfo methodDesc = helper.methodBuilder.interMethod(metadata)
                        .controllerMethod(method)
                        .serverMethod(serverMethod)
                        .parameterResolver(methodInfo.getParameterResolver())
                        .build();
                
                CatServerProperty serverProperty = helper.serverProperty;
                interceptor = new CatMethodAopInterceptor(serverProperty.getServerBean(), serverProperty.getServerInfo(), methodDesc);

                helper.interceptorMap.put(serverMethod, interceptor);
                return interceptor;

            } else if ( serverPropertyMethodName.equals(method.getName()) ){ // 返回CatServer类的class
                return new PropertyMethodInterceptor(helper.serverProperty);
            }

            // object默认方法
            return helper.enhancerDepend.getObjectMethodInterceptor();
        }

        @Override
        public int accept(Method method) {
            return this.methodIndexMap.get(method);
        }
        
        public Callback[] getCallbacks() {
            return this.callbacks.toArray(new Callback[callbacks.size()]);
        }

        public Class[] getCallbackTypes() {
            return this.callbackTypes.toArray(new Class[callbackTypes.size()]);
        }
    }

    
    /**
     * 代理类，返回一些自定义数据
     * */
    private static class PropertyMethodInterceptor implements MethodInterceptor {
        private final CatServerProperty serverProperty;
        private PropertyMethodInterceptor(CatServerProperty serverProperty) {
            this.serverProperty = serverProperty;
        }
        @Override
        public Object intercept(Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            return serverProperty;
        }
    }


    private static class ControllerNamingPolicy extends DefaultNamingPolicy {
        
        private final Class serverClass;
        private ControllerNamingPolicy(Class serverClass) {
            this.serverClass = serverClass;
        }

        public String getClassName(String prefix, String source, Object key, Predicate names) {
            if ( prefix != null && prefix.startsWith("java")) {
                return super.getClassName(prefix, source, key, names);
            }
            String base = serverClass.getPackage().getName() + ".asm." +
                    serverClass.getSimpleName() + CatServerUtil.BRIDGE_NAME + "$" +
                    source.substring(source.lastIndexOf(".") + 1) + "$$" +
                    Math.abs(key.hashCode());
            String attempt = base;
            for(int suffix = 2; names.evaluate(attempt); attempt = base + "_" + (suffix ++)) {
                ;
            }
            return attempt;
        }
    }

    
}
