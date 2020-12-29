package com.bugcat.catserver.scanner;

import com.bugcat.catface.spi.ResponesWrapper;
import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.catserver.asm.CatAsm;
import com.bugcat.catserver.beanInfos.CatServerInfo;
import com.bugcat.catserver.handler.CatMethodInterceptor;
import com.bugcat.catserver.utils.CatServerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.cglib.proxy.CallbackHelper;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: bugcat
 * */
public class CatServerFactoryBean<T> extends AbstractFactoryBean<T> {

    @Autowired
    private WebApplicationContext context;
    
    private Class<T> clazz;

    @Override
    public Class<T> getObjectType() {
        return clazz;
    }

    @Override
    protected T createInstance() throws Exception {
        CatServerInfo serverInfo = buildServerInfo(clazz);
        return createCatServers(context, clazz, serverInfo);
    }

    
    public final static CatServerInfo buildServerInfo(Class inter) {
        AnnotationAttributes attributes = CatServerInfo.getAttributes(inter);
        CatServerInfo serverInfo = new CatServerInfo(attributes);
        return serverInfo;
    }

    
    
    /**
     * 解析interface方法，生成动态代理类
     */
    public final static <T> T createCatServers(WebApplicationContext context, Class<T> clazz, CatServerInfo catServerInfo) throws Exception {

        ClassLoader classLoader = context.getClassLoader();
        
        //类加载器
        CatAsm asm = new CatAsm(classLoader);

        // 被@CatServer标记的类，包含的所有interface
        List<Class> inters = new ArrayList<>();
        
        for (Class superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
            for ( Class inter : superClass.getInterfaces() ) {
                inters.add(inter);
            }
        }
        
        Class warp = null;  //@CatServer上设置的统一响应包裹类
        Class<? extends ResponesWrapper> wrapper = catServerInfo.getWrapper();
        if( wrapper != null ){
            ResponesWrapper responesWrapper = ResponesWrapper.getResponesWrapper(wrapper);
            warp = responesWrapper.getWrapperClass();
        }
        
        Class[] thisInters = new Class[inters.size()];
        Map<String, StandardMethodMetadata> methodInfoMap = new HashMap<>();
        
        for(int i = 0; i < inters.size(); i ++ ){ // 遍历每个interface
            Class inter = inters.get(i);
            
            Method[] methods = inter.getMethods();
            for(Method method : methods){ // 遍历每个interface的方法，筛选只有包含CatServerInitBean.annName注解的
                StandardMethodMetadata metadata = new StandardMethodMetadata(method);
                Map<String, Object> attr = metadata.getAnnotationAttributes(CatServerInitBean.annName);
                if( attr != null ){
                    methodInfoMap.put(CatToosUtil.signature(method), metadata);
                }
            }
            Class enhancer = asm.enhancer(inter, warp);
            thisInters[i] = enhancer;
        }
        

        Map<String, CatMethodInterceptor> methodInterceptorMap = new HashMap<>();
        CallbackHelper helper = new CallbackHelper(clazz, thisInters) {
            @Override
            protected Object getCallback (Method method) {
                String name = method.getName();
                if ( name.startsWith(CatServerInitBean.bridgeName) ) {

                    String realName = name.substring(CatServerInitBean.bridgeName.length());
                    CatMethodInterceptor interceptor = methodInterceptorMap.get(realName);
                    if( interceptor == null ){
                        interceptor = new CatMethodInterceptor(catServerInfo);
                        methodInterceptorMap.put(realName, interceptor);
                    }
                    return interceptor;

                } else {

                    StandardMethodMetadata metadata = methodInfoMap.get(CatToosUtil.signature(method));
                    if ( metadata != null ) {  //如果方法上有 CatMethod
                        CatMethodInterceptor interceptor = methodInterceptorMap.get(name);
                        if( interceptor == null ){
                            interceptor = new CatMethodInterceptor(catServerInfo);
                            methodInterceptorMap.put(name, interceptor);
                        }
                        interceptor.setInterMethods(metadata);
                        interceptor.setRealMethod(method);
                        return interceptor;

                    } else {
                        return new MethodInterceptor() {    //默认方法
                            @Override
                            public Object intercept (Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                                return methodProxy.invokeSuper(target, args);
                            }
                        };
                    }
                }
            }
        };
        
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setInterfaces(thisInters);
        enhancer.setCallbackFilter(helper);
        enhancer.setCallbacks(helper.getCallbacks());
        enhancer.setClassLoader(classLoader);
        
        Object obj = enhancer.create();
        Class<?> server = obj.getClass();

        /**
         * 通过动态代理生成的obj，不会自动注入属性，需要借助Srping容器实现自动注入
         * */
        AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
        bpp.setBeanFactory(context.getAutowireCapableBeanFactory());
        bpp.processInjection(obj);
        
        
        methodInterceptorMap.forEach((key, value) -> {
            value.setServerClass(server);
            CatServerUtil.addInitBean(value); 
        });
        
        return (T) obj;
  
    }

    public Class<T> getClazz() {
        return clazz;
    }
    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    
}
