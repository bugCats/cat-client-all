package com.bugcat.catserver.scanner;

import com.bugcat.catface.utils.CatToosUtil;
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
            return registerCatServers(context, clazz, catServerInfo);
        } catch ( Exception ex ) {
            if( ex instanceof NoSuchBeanDefinitionException ){
                log.error("catserver 类被循环循环嵌套引用", ex);
            }
            throw ex;
        }
    }

    
    /**
     * 解析interface方法，生成动态代理类
     */
    public final static <T> T registerCatServers(WebApplicationContext context, Class<T> clazz, CatServerInfo catServerInfo) {
        
        List<Class> inters = new ArrayList<>();
        Class superClass = clazz;
        while ( superClass != Object.class ) {
            for(Class superInter : superClass.getInterfaces() ){
                inters.add(superInter);
            }
            superClass = superClass.getSuperclass();
        }
        
        Map<String, List<StandardMethodMetadata>> superMethods = new HashMap<>();
        for(Class inter : inters){
            Method[] methods = inter.getMethods();
            for(Method method : methods){
                StandardMethodMetadata metadata = new StandardMethodMetadata(method);
                Map<String, Object> attr = metadata.getAnnotationAttributes(CatServerInitBean.annName);
                if( attr != null ){
                    String signature = CatToosUtil.signature(method);
                    List<StandardMethodMetadata> list = superMethods.get(signature);
                    if( list == null ){
                        list = new ArrayList<>();
                        superMethods.put(signature, list);
                    }
                    list.add(metadata);
                }
            }
        }

        Class[] interfaces = clazz.getInterfaces();
        
        CallbackHelper helper = new CallbackHelper(clazz, interfaces) {
            @Override
            protected Object getCallback (Method method) {
                List<StandardMethodMetadata> interMethods = superMethods.get(CatToosUtil.signature(method));
                if ( interMethods != null ) {  //如果方法上有 CatMethod
                    
                    CatMethodInterceptor interceptor = new CatMethodInterceptor(catServerInfo, interMethods);//代理方法=aop
                    CatServerUtil.addInitBean(interceptor);
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
        };
            
        Enhancer enhancer = new Enhancer();
        enhancer.setInterfaces(interfaces);
        enhancer.setSuperclass(clazz);
        enhancer.setCallbackFilter(helper);
        enhancer.setCallbacks(helper.getCallbacks());
        Object obj = enhancer.create();

        AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
        bpp.setBeanFactory(context.getAutowireCapableBeanFactory());
        bpp.processInjection(obj);
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
