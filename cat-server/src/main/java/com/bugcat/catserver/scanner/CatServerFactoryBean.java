package com.bugcat.catserver.scanner;

import com.bugcat.catface.spi.ResponesWrapper;
import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.catserver.asm.CatAsm;
import com.bugcat.catserver.beanInfos.CatBridgeMethodInfo;
import com.bugcat.catserver.beanInfos.CatServerInfo;
import com.bugcat.catserver.handler.CatMethodInterceptor;
import com.bugcat.catserver.utils.CatServerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.cglib.proxy.CallbackHelper;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: bugcat
 * */
public class CatServerFactoryBean<T> extends AbstractFactoryBean<T> {

    private static Logger log = LoggerFactory.getLogger(CatServerScannerRegistrar.class);

    @Autowired
    private WebApplicationContext context;
    
    private CatServerInfo catServerInfo;
    private Class<T> clazz;


    @Override
    public Class<T> getObjectType() {
        return clazz;
    }

    
    
    @Override
    protected T createInstance() throws Exception {
        try {
            return createCatServers(context, clazz, catServerInfo);
        } catch ( Exception ex ) {
            ex.printStackTrace();
            if( ex instanceof NoSuchBeanDefinitionException ){
                log.error("catserver 类被循环循环嵌套引用", ex);
            }
            throw ex;
        }
    }

    
    /**
     * 解析interface方法，生成动态代理类
     */
    public final static <T> T createCatServers(WebApplicationContext context, Class<T> clazz, CatServerInfo catServerInfo) throws Exception {

        ClassLoader classLoader = context.getClassLoader();
        
        //类加载器
        CatAsm asm = new CatAsm(classLoader);

        // 被@CatServer标记的类，包含的所有interface
        Class[] inters = clazz.getInterfaces();
        
        /**
         * 最终需要桥连的方法
         * 如果clazz有多个interface，interface中出现了相同的方法
         * 只取其中一个
         * todo 不建议一个Service同时实现多个interface
         * */
        
        
        Class warp = null;  //@CatServer上设置的统一响应包裹类
        Class<? extends ResponesWrapper> wrapper = catServerInfo.getWrapper();
        if( wrapper != null ){
            ResponesWrapper responesWrapper = ResponesWrapper.getResponesWrapper(wrapper);
            warp = responesWrapper.getWrapperClass();
        }
        
        Class[] thisInters = new Class[inters.length];
        Map<String, CatBridgeMethodInfo> methodInfoMap = new HashMap<>();
        
        for(int i = 0; i < inters.length; i ++ ){ // 遍历每个interface
            Class inter = inters[i];
            
            Method[] methods = inter.getMethods();
            for(Method method : methods){ // 遍历每个interface的方法，筛选只有包含CatServerInitBean.annName注解的
                StandardMethodMetadata metadata = new StandardMethodMetadata(method);
                Map<String, Object> attr = metadata.getAnnotationAttributes(CatServerInitBean.annName);
                if( attr != null ){
                    CatBridgeMethodInfo info = new CatBridgeMethodInfo(metadata);
                    methodInfoMap.put(info.getSign(), info);
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

                    CatBridgeMethodInfo info = methodInfoMap.get(CatToosUtil.signature(method));
                    if ( info != null ) {  //如果方法上有 CatMethod

                        CatMethodInterceptor interceptor = methodInterceptorMap.get(name);
                        if( interceptor == null ){
                            interceptor = new CatMethodInterceptor(catServerInfo);
                            methodInterceptorMap.put(name, interceptor);
                        }
                        interceptor.setInterMethods(info.getMetadata());
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


    public CatServerInfo getCatServerInfo() {
        return catServerInfo;
    }
    public void setCatServerInfo(CatServerInfo catServerInfo) {
        this.catServerInfo = catServerInfo;
    }

    public Class<T> getClazz() {
        return clazz;
    }
    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    
}
