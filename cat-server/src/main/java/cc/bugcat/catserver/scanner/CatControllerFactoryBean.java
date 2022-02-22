package cc.bugcat.catserver.scanner;

import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.asm.CatAsmResult;
import cc.bugcat.catserver.asm.CatInterfaceEnhancer;
import cc.bugcat.catserver.asm.CatAsmMethod;
import cc.bugcat.catserver.beanInfos.CatServerInfo;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.handler.CatMethodBuilderFactory;
import cc.bugcat.catserver.handler.CatParameterResolverStrategy;
import cc.bugcat.catserver.utils.CatServerUtil;
import org.springframework.cglib.proxy.CallbackHelper;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.reflect.Method;
import java.util.*;

/**
 * controller对象工厂
 *
 *
 * @author bugcat
 * */
public class CatControllerFactoryBean implements Comparable<CatControllerFactoryBean> {

    /**
     * 原始的被@CatServer标记的类
     * */
    private final Class serverClass;

    /**
     * {@code @CatServer}注解信息
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
     * controller方法，与原serverClass方法映射
     * */
    private final Set<Method> bridgeMethods;


    private CatControllerFactoryBean(Builder builder) {
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




    public static Builder builder(){
        return new Builder();
    }


    public static class Builder {

        /**
         * 原始的被@CatServer标记的类
         * */
        private Class serverClass;

        /**
         * {@code @CatServer}注解信息
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
         * interface增强后结果缓存，防止同一个interface被反复增强
         * */
        private Map<Class, CatAsmResult> controllerCache;

        /**
         * object方法拦截器
         * */
        private MethodInterceptor defaultInterceptor;

        /**
         * 全局配置
         * */
        private CatServerConfiguration serverConfig;


        public Builder serverClass(Class serverClass) {
            this.serverClass = serverClass;
            return this;
        }

        public Builder serverConfig(CatServerConfiguration serverConfig) {
            this.serverConfig = serverConfig;
            return this;
        }

        public Builder controllerCache(Map<Class, CatAsmResult> controllerCache) {
            this.controllerCache = controllerCache;
            return this;
        }

        public Builder defaultInterceptor(MethodInterceptor defaultInterceptor) {
            this.defaultInterceptor = defaultInterceptor;
            return this;
        }


        public CatControllerFactoryBean build() throws Exception {

            this.serverInfo = CatServerInfo.build(serverClass, serverConfig);
            this.controller = createController();

            for (Class superClass = serverClass; superClass != Object.class; superClass = superClass.getSuperclass() ) {
                level = level + 1;
            }

            Class thisClazz = controller.getClass(); //cglib动态生成的class => interface的实现类
            Class[] interfaces = thisClazz.getInterfaces();
            for( Class interfaceClass : interfaces ){ //增强后的interface
                if ( CatInterfaceEnhancer.isBridgeClass(interfaceClass) ) {
                    for( Method method : interfaceClass.getMethods() ){
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

            ClassLoader classLoader = CatServerUtil.getClassLoader();

            //类加载器
            CatInterfaceEnhancer serverAsm = new CatInterfaceEnhancer();

            // 被@CatServer标记的类，包含的所有interface
            List<Class> interfaces = new ArrayList<>();

            for ( Class superClass = serverClass; superClass != Object.class; superClass = superClass.getSuperclass()) {
                for ( Class interfaceClass : superClass.getInterfaces() ) {
                    interfaces.add(interfaceClass);
                }
            }

            Class[] thisInters = new Class[interfaces.size()];

            final Map<String, Method> serverMethodMap = new HashMap<>();
            for ( Method method : serverClass.getMethods() ) {
                String signatureId = CatServerUtil.signatureId(method);
                serverMethodMap.put(signatureId, method);
            }

            // 增强前的interface方法签名id
            final Map<String, StandardMethodMetadata> metadataMap = new HashMap<>();

            // 增强后的interface方法签名id：CatMethodInfo
            final Map<String, CatAsmMethod> allMethodInfoMap = new HashMap<>();

            for( int idx = interfaces.size() - 1; idx >= 0; idx -- ){ // 遍历每个interface
                Class interfaceClass = interfaces.get(idx);

                CatAsmResult asmResult = controllerCache.get(interfaceClass);
                if( asmResult == null ){
                    asmResult = serverAsm.enhancer(interfaceClass, serverInfo); //使用asm增强interface
                    controllerCache.put(interfaceClass, asmResult);
                }

                allMethodInfoMap.putAll(asmResult.getMethodInfoMap());
                thisInters[idx] = asmResult.getEnhancerClass();

                for(Method method : interfaceClass.getMethods()){
                    if( CatToosUtil.isObjectMethod(CatToosUtil.signature(method)) ){
                        continue;
                    }
                    String signatureId = CatServerUtil.signatureId(method);
                    StandardMethodMetadata metadata = new StandardMethodMetadata(method);
                    metadataMap.put(signatureId, metadata);
                }
            }
            // 此时thisInters中，全部为增强后的扩展interface

            CatMethodBuilderFactory factory = CatMethodBuilderFactory.newFactory();
            factory.serverInfo(serverInfo).serverClass(serverClass);

            CallbackHelper helper = new CallbackHelper(Object.class, thisInters) {
                @Override
                protected Object getCallback (Method method) {

                    // method为增强后的方法
                    String signatureId = CatServerUtil.signatureId(method);
                    CatAsmMethod methodInfo = allMethodInfoMap.get(signatureId);
                    if ( methodInfo != null ){
                        StandardMethodMetadata metadata = metadataMap.get(methodInfo.getInterfaceSignatureId());
                        if ( metadata != null ) {//原interface方法

                            Method serverMethod = serverMethodMap.get(methodInfo.getInterfaceSignatureId());
                            CatParameterResolverStrategy resolver = methodInfo.getResolverStrategy();
                            factory.interMethodMetadata(metadata)
                                    .serverMethod(serverMethod)
                                    .parameterResolver(resolver.createParameterResolver());

                            return factory.build();
                        }
                    }

                    return defaultInterceptor;
                }
            };

            Enhancer enhancer = new Enhancer();
            enhancer.setClassLoader(classLoader);
            enhancer.setSuperclass(Object.class);
            enhancer.setInterfaces(thisInters);
            enhancer.setCallbackFilter(helper);
            enhancer.setCallbacks(helper.getCallbacks());

            Object controller = enhancer.create();
            return controller;
        }
    }

}
