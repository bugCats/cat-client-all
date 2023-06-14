package cc.bugcat.catserver.asm;

import cc.bugcat.catface.handler.EnvironmentAdapter;
import cc.bugcat.catserver.config.CatServerConfiguration;
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
     * interface增强后结果缓存，防止同一个interface被反复增强
     * */
    private final Map<Class, CatAsmInterface> controllerCache;

    /**
     * interface类解析后的信息
     * */
    private final Map<Class, AsmInterfaceDescriptor> classDescriptorMap;
    
    /**
     * 一些全局配置项
     * */
    private final CatServerConfiguration serverConfig;
    
    /**
     * 环境变量
     * */
    private final EnvironmentAdapter envProp;
    
    
    public CatEnhancerDepend(CatServerConfiguration serverConfig, EnvironmentAdapter envProp, int serverSize) {
        this.objectMethodInterceptor = new MethodInterceptor() {
            @Override
            public Object intercept (Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                return methodProxy.invokeSuper(target, args);
            }
        };
        this.controllerCache = new HashMap<>(serverSize * 2);
        this.classDescriptorMap = new HashMap<>(serverSize * 4);
        this.serverConfig = serverConfig;
        this.envProp = envProp;
    }

    
    public CatAsmInterface getControllerDescriptor(Class interfaceClass) {
        return controllerCache.get(interfaceClass);
    }
    public void putControllerDescriptor(Class interfaceClass, CatAsmInterface asmResult) {
        controllerCache.put(interfaceClass, asmResult);
    }
    
    public AsmInterfaceDescriptor getClassDescriptor(Class interfaceClass) {
        return classDescriptorMap.get(interfaceClass);
    }
    public void putClassDescriptor(Class interfaceClass, AsmInterfaceDescriptor classDescriptor) {
        classDescriptorMap.put(interfaceClass, classDescriptor);
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
}
