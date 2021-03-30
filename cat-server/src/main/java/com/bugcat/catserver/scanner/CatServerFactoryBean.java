package com.bugcat.catserver.scanner;

import com.bugcat.catface.spi.ResponesWrapper;
import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.catserver.asm.CatAsm;
import com.bugcat.catserver.beanInfos.CatServerInfo;
import com.bugcat.catserver.handler.CatInterceptorMethodBuilder;
import com.bugcat.catserver.utils.CatServerUtil;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.cglib.core.DefaultNamingPolicy;
import org.springframework.cglib.core.Predicate;
import org.springframework.cglib.proxy.CallbackHelper;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: bugcat
 * */
public class CatServerFactoryBean<T> extends AbstractFactoryBean<T>{
    
    
    private Class<T> clazz;

    
    @Override
    public Class<T> getObjectType() {
        return clazz;
    }

    
    
    @Override
    protected T createInstance() throws Exception {
        CatServerInfo serverInfo = CatServerInfo.buildServerInfo(clazz);
        return createCatServers(clazz, serverInfo);
    }
    
    
    /**
     * 解析interface方法，生成动态代理类
     */
    public final static <T> T createCatServers(Class<T> clazz, CatServerInfo catServerInfo) throws Exception {

        Class warp = null;  //@CatServer上设置的统一响应包装器类
        Class<? extends ResponesWrapper> wrapper = catServerInfo.getWrapper();
        if( wrapper != null ){
            ResponesWrapper responesWrapper = ResponesWrapper.getResponesWrapper(wrapper);
            warp = responesWrapper.getWrapperClass();
        }

        ClassLoader classLoader = CatServerUtil.getClassLoader();

        //类加载器
        CatAsm asm = new CatAsm(classLoader);
        
        // 被@CatServer标记的类，包含的所有interface
        List<Class> inters = new ArrayList<>();
        for (Class superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
            for ( Class inter : superClass.getInterfaces() ) {
                inters.add(inter);
            }
        }
        
        Map<String, StandardMethodMetadata> metadataMap = new HashMap<>();
        Class[] thisInters = new Class[inters.size()];
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

        
        CallbackHelper helper = new CallbackHelper(Object.class, thisInters) {
            @Override
            protected Object getCallback (Method method) {
                StandardMethodMetadata metadata = metadataMap.get(CatToosUtil.signature(method));
                if ( metadata != null ) {
                    CatInterceptorMethodBuilder builder = CatInterceptorMethodBuilder.builder();
                    builder.interMethod(metadata);
                    return builder.build(catServerInfo); // ;
                } else {
                    return new MethodInterceptor() {    //默认方法
                        @Override
                        public Object intercept (Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                            return methodProxy.invokeSuper(target, args);
                        }
                    };
                }
            }
        };
        
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Object.class);
        enhancer.setInterfaces(thisInters);
        enhancer.setCallbackFilter(helper);
        enhancer.setCallbacks(helper.getCallbacks());
        
        Object ctrl = enhancer.create();
        CatServerUtil.setCtrlClass(clazz, ctrl);
        
        AutowireCapableBeanFactory beanFactory = CatServerUtil.getBeanFactory();
        Object impl = beanFactory.autowire(clazz, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);

        return (T) impl;
    }

    
    
    public Class<T> getClazz() {
        return clazz;
    }
    
    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }
    
    

}
