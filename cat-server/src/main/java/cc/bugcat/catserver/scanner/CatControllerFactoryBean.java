package cc.bugcat.catserver.scanner;

import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.annotation.CatServer;
import cc.bugcat.catserver.asm.CatAsmMethod;
import cc.bugcat.catserver.asm.CatAsmResult;
import cc.bugcat.catserver.asm.CatEnhancerDepend;
import cc.bugcat.catserver.asm.CatInterfaceEnhancer;
import cc.bugcat.catserver.asm.CatServerHandler;
import cc.bugcat.catserver.beanInfos.CatServerInfo;
import cc.bugcat.catserver.handler.CatMethodAopInterceptor;
import cc.bugcat.catserver.handler.CatMethodInfo;
import cc.bugcat.catserver.handler.CatMethodInfoBuilder;
import cc.bugcat.catserver.handler.CatParameterResolverStrategy;
import cc.bugcat.catserver.utils.CatServerUtil;
import org.springframework.cglib.core.DefaultNamingPolicy;
import org.springframework.cglib.core.Predicate;
import org.springframework.cglib.proxy.CallbackHelper;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * controller对象工厂。
 * 先增强interface，然后根据增强后的Interface使用cglib动态生成controller对象。
 *
 * @author bugcat
 * */
public class CatControllerFactoryBean implements Comparable<CatControllerFactoryBean> {

    
    /**
     * 从controller中获取被@CatServer注解的对象
     * {@link CatServerHandler#getCatServerClass()}
     * */
    private final static String catServerClass = "getCatServerClass";

    
    /**
     * 原始的被@CatServer标记的类
     * */
    private final Class serverClass;

    
    /**
     * {@link @CatServer}注解信息
     * */
    private final CatServerInfo serverInfo;

    
    /**
     * 通过动态代理生成的controller对象
     * */
    private final Object controller;

    
    /**
     * serverClass类的继承关系
     * 如果是子类，那么level比父类大，排在后面
     * */
    private int level = 0;

    
    /**
     * CatServer类的实现方法
     * */
    private final Set<Method> bridgeMethods;
    
    
    
    private CatControllerFactoryBean(Factory builder) {
        this.serverClass = builder.serverClass;
        this.serverInfo = builder.serverInfo;
        this.controller = builder.controller;
        this.bridgeMethods = builder.bridgeMethods;
        this.level = builder.level;
    }


    @Override
    public int compareTo(CatControllerFactoryBean info) {
        return level - info.level;
    }


    public Class getServerClass() {
        return serverClass;
    }
    public CatServerInfo getServerInfo() {
        return serverInfo;
    }
    public Object getController() {
        return controller;
    }
    public Set<Method> getBridgeMethods() {
        return bridgeMethods;
    }



    /**********************************************************************************/
    

    public static Factory newFactory(){
        return new Factory();
    }


    public static class Factory {
        
        /**
         * {@link CatServer}注解信息
         * */
        private CatServerInfo serverInfo;

        /**
         * 通过动态代理生成的controller对象
         * */
        private Object controller;

        /**
         * 增强后的interface方法
         * */
        private Set<Method> bridgeMethods = new HashSet<>();

        /**
         * 当前CatServer类的继承层数
         * */
        private int level = 0;

        
        /**
         * 被@CatServer标记的原始的实现类
         * */
        private Class serverClass;
        
        /**
         * 全局配置项与缓存
         * */
        private CatEnhancerDepend enhancerDepend;
        
        public Factory serverClass(Class serverClass) {
            this.serverClass = serverClass;
            return this;
        }
        public Factory enhancerDepend(CatEnhancerDepend enhancerDepend){
            this.enhancerDepend = enhancerDepend;
            return this;
        }
        
        
        public CatControllerFactoryBean createBean() throws Exception {
            
            // CatServer注解信息
            this.serverInfo = CatServerInfo.build(serverClass, enhancerDepend);
            
            // 1. 增强被实现的interface
            // 2. 通过增强Interface动态代理生成controller
            this.controller = createController();

            // 当前CatServer类的继承层数，继承层级越多，level越大
            for (Class superClass = serverClass; superClass != Object.class; superClass = superClass.getSuperclass() ) {
                level = level + 1;
            }
            
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

            return new CatControllerFactoryBean(this);
        }


        
        
        /**
         * 通过serverClass、与@CatServer注解，动态生成controller对象
         * */
        private Object createController() throws Exception {

            //类加载器
            ClassLoader classLoader = CatServerUtil.getClassLoader();

            CatInterfaceEnhancer serverAsm = new CatInterfaceEnhancer();

            // 被@CatServer标记的类，包含的所有原始interface
            Stack<Class> interfaces = new Stack<>();
            for ( Class superClass = serverClass; superClass != Object.class; superClass = superClass.getSuperclass()) {
                for ( Class interfaceClass : superClass.getInterfaces() ) {
                    interfaces.add(interfaceClass);
                }
            }

            // 增强后的Interface
            int lengthPointer = 0;
            Class[] thisInters = new Class[interfaces.size() + 1];
            thisInters[lengthPointer ++ ] = CatServerHandler.class;

            
            // 缓存原始CatServer类的method签名，最后需要与增强后的Method对应
            final Map<String, Method> serverMethodMap = new HashMap<>(serverClass.getMethods().length * 2);
            for ( Method method : serverClass.getMethods() ) {
                String signatureId = CatServerUtil.typeSignatureId(method);
                serverMethodMap.put(signatureId, method);
            }

            
            // 增强前的interface方法签名id => 原始方法
            final Map<String, StandardMethodMetadata> metadataMap = new HashMap<>();

            // 增强后的Interface方法签名id => CatMethodInfo
            final Map<String, CatAsmMethod> allMethodInfoMap = new HashMap<>();

            Map<Class, CatAsmResult> controllerCache = enhancerDepend.getControllerCache();
            
            for (Class interClass : interfaces ){ // 遍历每个interface
                
                CatAsmResult asmResult = controllerCache.get(interClass);
                if( asmResult == null ){
                    asmResult = serverAsm.enhancer(interClass, serverInfo, enhancerDepend); //使用asm增强interface
                    controllerCache.put(interClass, asmResult);
                    allMethodInfoMap.putAll(asmResult.getMethodInfoMap());
                }
                thisInters[lengthPointer ++] = asmResult.getEnhancerClass(); // 增强后的Interface

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

            CatMethodInfoBuilder infoBuilder = CatMethodInfoBuilder.builder(serverClass, serverInfo);
            
            
            CallbackHelper helper = new CallbackHelper(Object.class, thisInters) {
                @Override
                protected Object getCallback (Method method) {

                    // method为增强后的方法
                    String signatureId = CatServerUtil.typeSignatureId(method);
                    CatAsmMethod methodInfo = allMethodInfoMap.get(signatureId);
                    if ( methodInfo != null ){
                        StandardMethodMetadata metadata = metadataMap.get(methodInfo.getInterfaceSignatureId());
                        if ( metadata != null ) {//原interface方法
                            
                            //原始CatServer类的方法
                            Method serverMethod = serverMethodMap.get(methodInfo.getInterfaceSignatureId());
                            CatParameterResolverStrategy resolver = methodInfo.getResolverStrategy();
                            
                            CatMethodInfo methodDesc = infoBuilder.interMethod(metadata)
                                    .controllerMethod(method)
                                    .serverMethod(serverMethod)
                                    .parameterResolver(resolver.createParameterResolver())
                                    .build();

                            return CatMethodAopInterceptor.builder()
                                    .methodInfo(methodDesc)
                                    .serverInfo(serverInfo)
                                    .serverBean(infoBuilder.getServerBean())
                                    .build();
                        }
                    } else if ( catServerClass.equals(method.getName()) ){ // 返回CatServer类的class
                        return new MethodInterceptor() {
                            @Override
                            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                                return serverClass;
                            }
                        };
                    }
                    
                    // object默认方法
                    return enhancerDepend.getObjectMethodInterceptor();
                }
            };
            
            Enhancer enhancer = new Enhancer();
            enhancer.setClassLoader(classLoader);
            enhancer.setSuperclass(Object.class);
            enhancer.setInterfaces(thisInters);
            enhancer.setCallbackFilter(helper);
            enhancer.setCallbacks(helper.getCallbacks());
            enhancer.setNamingPolicy(new DefaultNamingPolicy(){
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
            });
            
            Object controller = enhancer.create(); // 一定是CatServerHandler的子类
            return controller;
        }
    }
    
}
