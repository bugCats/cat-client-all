package cc.bugcat.catserver.asm;

import cc.bugcat.catface.handler.EnvironmentAdapter;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.handler.CatMethodAopInterceptor;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * asm相关缓存
 * */
public class CatEnhancerDepend {

    /**
     * object默认方法拦截器
     * */
    private final MethodInterceptor objectMethodInterceptor;
    
    /**
     * 一些全局配置项
     * */
    private final CatServerConfiguration serverConfig;
    
    /**
     * 环境变量
     * */
    private final EnvironmentAdapter envProp;
    
    /**
     * CatServer-interface 与对应方法拦截器
     * */
    private final Map<Method, CatMethodAopInterceptor> interceptorMap = new HashMap<>();

    /**
     * interface增强后结果缓存，防止同一个interface被反复增强
     * */
    private final Map<Class, CatAsmInterface> ctrlAsmMap;

    /**
     * interface类解析后的信息
     * */
    private final Map<Class, AsmInterfaceDescriptor> classDescriptorMap;
    
    
    public CatEnhancerDepend(CatServerConfiguration serverConfig, EnvironmentAdapter envProp, int serverSize) {
        this.objectMethodInterceptor = new DefaultMethodInterceptor();
        this.ctrlAsmMap = new HashMap<>(serverSize * 2);
        this.classDescriptorMap = new HashMap<>(serverSize * 4);
        this.serverConfig = serverConfig;
        this.envProp = envProp;
    }

    public MethodInterceptor getObjectMethodInterceptor() {
        return objectMethodInterceptor;
    }
    public CatServerConfiguration getServerConfig() {
        return serverConfig;
    }
    public EnvironmentAdapter getEnvironmentAdapter() {
        return envProp;
    }


    public void clear(){
        ctrlAsmMap.clear();
        classDescriptorMap.clear();
        interceptorMap.clear();
    }


    public CatAsmInterface getControllerDescriptor(Class interfaceClass) {
        return ctrlAsmMap.get(interfaceClass);
    }
    public void putControllerDescriptor(Class interfaceClass, CatAsmInterface asmResult) {
        ctrlAsmMap.put(interfaceClass, asmResult);
    }

    public AsmInterfaceDescriptor getClassDescriptor(Class interfaceClass) {
        return classDescriptorMap.get(interfaceClass);
    }
    public void putClassDescriptor(Class interfaceClass, AsmInterfaceDescriptor classDescriptor) {
        classDescriptorMap.put(interfaceClass, classDescriptor);
    }

    public Map<Method, CatMethodAopInterceptor> getInterceptorMap() {
        return interceptorMap;
    }

    
    /**
     * 默认的拦截器
     * */
    private static class DefaultMethodInterceptor implements MethodInterceptor {
        @Override
        public Object intercept (Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            return methodProxy.invokeSuper(target, args);
        }
    }
}
