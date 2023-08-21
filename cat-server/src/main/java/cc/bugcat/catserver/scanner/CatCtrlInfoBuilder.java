package cc.bugcat.catserver.scanner;

import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.annotation.CatServer;
import cc.bugcat.catserver.asm.CatAsmInterface;
import cc.bugcat.catserver.asm.CatAsmMethod;
import cc.bugcat.catserver.asm.CatEnhancerDepend;
import cc.bugcat.catserver.asm.CatInterfaceEnhancer;
import cc.bugcat.catserver.asm.CatServerInstance;
import cc.bugcat.catserver.asm.CatServerProperty;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.handler.CatMethodAopInterceptor;
import cc.bugcat.catserver.handler.CatMethodInfo;
import cc.bugcat.catserver.handler.CatMethodInfoBuilder;
import cc.bugcat.catserver.handler.CatMethodInfoBuilder.BuilderFactory;
import cc.bugcat.catserver.handler.CatServerDepend;
import cc.bugcat.catserver.handler.CatServerInfo;
import cc.bugcat.catserver.spi.CatInterceptorGroup;
import cc.bugcat.catserver.spi.CatServerInterceptor;
import cc.bugcat.catserver.utils.CatServerUtil;
import org.springframework.cglib.core.DefaultNamingPolicy;
import org.springframework.cglib.core.Predicate;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
     * 环境
     * */
    private final CatServerDepend serverDepend;
    
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
    



    protected CatCtrlInfoBuilder(Class serverClass, CatServerDepend serverDepend, CatEnhancerDepend enhancerDepend) {
        this.serverClass = serverClass;
        this.serverDepend = serverDepend;
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
        this.serverInfo = CatServerInfo.build(serverClass, serverDepend);
        
        // 1. 增强被实现的interface
        // 2. 通过增强Interface动态代理生成controller
        this.controller = createController();

        final Map<String, Method> serverClassMethodMap = new HashMap<>(serverClass.getMethods().length * 2);
        for ( Method method : serverClass.getMethods() ) {
            if ( serverClass.equals(method.getDeclaringClass()) ) {
                String signatureId = CatServerUtil.methodSignature(method);
                serverClassMethodMap.put(signatureId, method); //包含父类没有重写的方法
            }
        }
        
        //cglib动态生成的class => Interface的实现类
        Class thisClazz = controller.getClass(); 
        Class[] interfaces = thisClazz.getInterfaces();
        
        for( Class interfaceClass : interfaces ){ //增强后的interface
            if ( CatInterfaceEnhancer.isBridgeClass(interfaceClass) ) { //判断是否为增强后的Interface
                for( Method method : interfaceClass.getMethods() ){ // 增强后的Method
                    String signatureId = CatServerUtil.methodSignature(method);
                    if( serverInfo.isCatface() || serverClassMethodMap.containsKey(signatureId) ){
                        bridgeMethods.add(method);
                    }
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
            String signatureId = CatServerUtil.methodSignature(method);
            serverClassMethodMap.put(signatureId, method); //包含父类没有重写的方法
        }

        
        // 原interface方法签名id => interface的原始方法
        final Map<String, StandardMethodMetadata> interfaceMap = new HashMap<>();

        // 增强后的Interface方法签名id => CatMethodInfo
        final Map<String, CatAsmMethod> asmMethodInfoMap = new HashMap<>();

        for (Class interClass : interfaces ){ // 遍历CatServer类的每个interface
            CatAsmInterface asmInterface = enhancerDepend.getControllerDescriptor(interClass);
            if( asmInterface == null ){
                asmInterface = serverAsm.enhancer(interClass, serverInfo, enhancerDepend); //使用asm增强interface
                enhancerDepend.putControllerDescriptor(interClass, asmInterface); //缓存interface，避免重复增强
            }
            asmMethodInfoMap.putAll(asmInterface.getMethodInfoMap());
            thisInters[lengthPointer ++] = asmInterface.getEnhancerClass(); // 增强后的Interface

            for(Method method : interClass.getMethods()){
                if( CatToosUtil.isObjectMethod(CatToosUtil.signature(method)) ){ // 是否为object默认方法
                    continue;
                }
                String signatureId = CatServerUtil.methodSignature(method);
                StandardMethodMetadata metadata = new StandardMethodMetadata(method);
                interfaceMap.put(signatureId, metadata);
            }
        }
        // 此时thisInters中，全部为增强后的扩展interface
        
        CatServerConfiguration serverConfig = serverDepend.getServerConfig();
        List<CatInterceptorGroup> interceptorGroup = new ArrayList<>(serverConfig.getInterceptorGroup()); //拦截器组
        boolean userOff = false;
        boolean groupOff = false;
        Set<Class<? extends CatServerInterceptor>> interceptorSet = new LinkedHashSet<>();
        for ( Class<? extends CatServerInterceptor> interceptor : serverInfo.getInterceptors() ) {
            if( interceptorSet.contains(interceptor) ){
                interceptorSet.remove(interceptor);
            }
            if( CatServerInterceptor.NoOp.class == interceptor ){
                userOff = true;
                continue;
            } else if ( CatServerInterceptor.GroupOff.class == interceptor ){
                groupOff = true;
                continue;
            }
            interceptorSet.add(interceptor);
        }
        if( groupOff ){ //关闭拦截器组
            interceptorGroup.clear();
        }
        List<CatServerInterceptor> handers = null;
        if( userOff ){ //关闭自定义和全局
            handers = new ArrayList<>(0);
        } else { //启用自定义、全局拦截器
            handers = new ArrayList<>(interceptorSet.size() + 1);
            for ( Class<? extends CatServerInterceptor> clazz : interceptorSet ) {
                if (CatServerInterceptor.class.equals(clazz) ) {
                    // 默认拦截器，使用CatServerConfiguration.getGlobalInterceptor()替换
                    handers.add(serverConfig.getServerInterceptor());

                } else {
                    // CatServer上自定义拦截器
                    handers.add(CatServerUtil.getBean(clazz));
                }
            }
            if( handers.size() == 0 ){ //如果没有配置拦截器，添加全局
                handers.add(serverConfig.getServerInterceptor());
            }
        }
        Collections.sort(interceptorGroup, Comparator.comparingInt(CatInterceptorGroup::getOrder));
        
        final List<CatServerInterceptor> interceptors = handers;    //自定义和全局拦截器
        final List<CatInterceptorGroup> interceptorGroups = interceptorGroup; //运行时拦截器组
        
        //被CatServer标记的类
        final Object serverBean = CatServerUtil.getBean(serverClass);
        final CatServerProperty serverProperty = new CatServerProperty(serverClass, serverBean, serverInfo);
        final BuilderFactory factory = CatMethodInfoBuilder.factory(serverProperty);
        final Map<Method, CatMethodAopInterceptor> interceptorMap = new HashMap<>();;

        final List<Method> ctrlMethods = new ArrayList<>();
        Enhancer.getMethods(Object.class, thisInters, ctrlMethods);
        for ( Method ctrlMethod : ctrlMethods ) {//所有增强之后的interface的方法
            String signatureId = CatServerUtil.methodSignature(ctrlMethod);
            CatAsmMethod methodInfo = asmMethodInfoMap.get(signatureId);
            if( methodInfo == null ){
                continue; //不在增强方法Map中，忽略
            }

            //如果是asm-interface的方法
            StandardMethodMetadata metadata = interfaceMap.get(methodInfo.getInterfaceSignatureId()); // interface的原始方法
            Method serverMethod = serverClassMethodMap.get(methodInfo.getInterfaceSignatureId()); //原始CatServer类的方法
            
            CatMethodInfo methodDesc = factory.builder()
                    .interfaceMethod(metadata)
                    .controllerMethod(ctrlMethod)
                    .serverMethod(serverMethod)
                    .build();

            CatMethodAopInterceptor interceptor = CatMethodAopInterceptor.builder()
                    .serverInfo(serverInfo)
                    .methodInfo(methodDesc)
                    .serverBean(serverBean)
                    .interceptorGroups(interceptorGroups)
                    .interceptors(interceptors)
                    .resultHandler(serverInfo.getResultHandler())
                    .parameterResolver(methodInfo.getParameterResolver())
                    .build();
            
            interceptorMap.put(ctrlMethod, interceptor);
        }
        
        CatCallbackHelper helper = new CatCallbackHelper()
                .setEnhancerDepend(enhancerDepend)
                .setInterceptorMap(interceptorMap)
                .setServerProperty(serverProperty);
        
        CatCallbackFilter filter = new CatCallbackFilter();
        filter.parse(helper, ctrlMethods);
        
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


        public CatCallbackHelper setEnhancerDepend(CatEnhancerDepend enhancerDepend) {
            this.enhancerDepend = enhancerDepend;
            return this;
        }
        public CatCallbackHelper setInterceptorMap(Map<Method, CatMethodAopInterceptor> interceptorMap){
            this.interceptorMap = interceptorMap;
            return this;
        }
        public CatCallbackHelper setServerProperty(CatServerProperty serverProperty) {
            this.serverProperty = serverProperty;
            return this;
        }
    }
    

    private static class CatCallbackFilter implements CallbackFilter {

        private Map<Method, Integer> methodIndexMap = new HashMap<>();
        private List<Callback> callbacks = new ArrayList<>();

        public void parse(CatCallbackHelper helper, List<Method> ctrlMethods) {
            Map<Callback, Integer> indexes = new HashMap();

            for(int index = 0, size = ctrlMethods.size(); index < size; index ++ ) {
                Method method = ctrlMethods.get(index); // method为增强后的方法 asm-method
                Callback callback = this.getCallback(helper, method);
                if (indexes.get(callback) == null) {
                    indexes.put(callback, index);
                }
                this.methodIndexMap.put(method, index);
                this.callbacks.add(callback);
            }
        }

        private Callback getCallback (CatCallbackHelper helper, Method method) { 
            if ( serverPropertyMethodName.equals(method.getName()) ){ // 返回CatServer类的class
                return new PropertyMethodInterceptor(helper.serverProperty);
            } else {
                CatMethodAopInterceptor interceptor = helper.interceptorMap.get(method);
                if( interceptor != null ){
                    return interceptor;
                } else {
                    // object默认方法
                    return helper.enhancerDepend.getObjectMethodInterceptor();
                }
            }
        }

        @Override
        public int accept(Method method) {
            return this.methodIndexMap.get(method);
        }
        
        public Callback[] getCallbacks() {
            return this.callbacks.toArray(new Callback[callbacks.size()]);
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
