package cc.bugcat.catserver.asm;

import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.utils.CatServerUtil;
import org.springframework.cglib.core.DefaultNamingPolicy;
import org.springframework.cglib.core.Predicate;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
    private final Map<Class, CatAsmResult> controllerCache;

    /**
     * interface类解析后的信息
     * */
    private final Map<Class, AsmClassDescriptor> classDescriptorMap;
    
    /**
     * 一些全局配置项
     * */
    private final CatServerConfiguration serverConfig;
    
    /**
     * 环境变量
     * */
    private final Properties envProp;
    
    
    public CatEnhancerDepend(Class<? extends CatServerConfiguration> configClass, int serverSize) {
        this.objectMethodInterceptor = new MethodInterceptor() {
            @Override
            public Object intercept (Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                return methodProxy.invokeSuper(target, args);
            }
        };
        this.controllerCache = new HashMap<>(serverSize * 2);
        this.classDescriptorMap = new HashMap<>(serverSize * 4);
        this.serverConfig = CatServerUtil.getBean(configClass);
        this.envProp = CatToosUtil.envProperty(CatServerUtil.getEnvironment());
    }
    
    
    public MethodInterceptor getObjectMethodInterceptor() {
        return objectMethodInterceptor;
    }
    public Map<Class, CatAsmResult> getControllerCache() {
        return controllerCache;
    }
    public CatServerConfiguration getServerConfig() {
        return serverConfig;
    }
    public AsmClassDescriptor getClassDescriptorMap(Class interfaceClass) {
        return classDescriptorMap.get(interfaceClass);
    }
    public Properties getEnvProp() {
        return envProp;
    }
}
