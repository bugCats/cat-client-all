package com.bugcat.catserver.handler;

import com.bugcat.catface.spi.ResponesWrapper;
import com.bugcat.catserver.beanInfos.CatServerInfo;
import com.bugcat.catserver.spi.CatInterceptor;
import com.bugcat.catserver.utils.CatServerUtil;
import org.springframework.asm.Type;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cglib.core.Signature;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.reflect.Method;
import java.util.*;


/**
 * 通过cglib生成代理类
 * 单例
 *
 * @author bugcat
 */
public final class CatInterceptorBuilders implements InitializingBean {

    
    private Map<String, MethodBuilder> builderMap = new HashMap<>();


    private Class serverClass;
    private CatServerInfo catServerInfo;
    

    private CatInterceptorBuilders() { }

    
    public static CatInterceptorBuilders builders(){
        return new CatInterceptorBuilders();
    }


    
    @Override
    public void afterPropertiesSet() throws Exception {
        builderMap.values().stream().forEach(builder -> {
            Method realMethod = builder.realMethod;
            Signature signature = new Signature(realMethod.getName(), Type.getReturnType(realMethod), Type.getArgumentTypes(realMethod));
            MethodProxy realMethodProxy = MethodProxy.find(serverClass, signature);

            Class<? extends CatInterceptor>[] handerList = catServerInfo.getHanders();
            List<CatInterceptor> handers = new ArrayList<>(handerList.length);
            for(Class<? extends CatInterceptor> clazz : handerList) {
                if( CatInterceptor.class.equals(clazz) ){
                    handers.add(CatInterceptor.instance);
                } else {
                    handers.add(CatServerUtil.getBean(clazz));
                }
            }
            handers.sort(Comparator.comparingInt(CatInterceptor::getOrder));

            ResponesWrapper wrapper = ResponesWrapper.getResponesWrapper(catServerInfo.getWrapper());


            builder.methodProxy = realMethodProxy;
            builder.handers = handers;
            builder.wrapper = wrapper;
            
            builder.catMethodInterceptor.initializing(builder);
        });
    }
    
    
    
    public MethodBuilder builder(String name){
        String key = CatServerUtil.trimName(name);
        MethodBuilder builder = builderMap.get(key);
        if( builder == null ){
            builder = new MethodBuilder();
            builderMap.put(key, builder);
        }
        return builder;
    }
    
    public MethodBuilder getBuilder(String name){
        return builderMap.get(CatServerUtil.trimName(name));
    }

    public void registryInitializingBean(Class<?> serverClass, CatServerInfo catServerInfo) {
        this.serverClass = serverClass;
        this.catServerInfo = catServerInfo;
        CatServerUtil.addInitBean(this);
    }


    public final static class MethodBuilder {
        
        private CatMethodInterceptor catMethodInterceptor;

        
        private StandardMethodMetadata interMethod;         //interface上对于的方法
        private Method realMethod;
        
        private MethodProxy methodProxy;
        private List<CatInterceptor> handers;
        private ResponesWrapper wrapper;
        
        
        private MethodBuilder(){}

        public MethodBuilder interMethod(StandardMethodMetadata interMethod) {
            this.interMethod = interMethod;
            return this;
        }
        public MethodBuilder realMethod(Method realMethod) {
            this.realMethod = realMethod;
            return this;
        }
        
        public CatMethodInterceptor build(){
            return catMethodInterceptor == null ? catMethodInterceptor = new CatMethodInterceptor() : catMethodInterceptor;
        }

        
        
        public StandardMethodMetadata getInterMethod() {
            return interMethod;
        }

        public Method getRealMethod() {
            return realMethod;
        }

        public MethodProxy getMethodProxy() {
            return methodProxy;
        }

        public List<CatInterceptor> getHanders() {
            return handers;
        }

        public ResponesWrapper getWrapper() {
            return wrapper;
        }
    }

    
    
}
