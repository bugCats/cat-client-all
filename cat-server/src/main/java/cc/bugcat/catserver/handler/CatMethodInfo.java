package cc.bugcat.catserver.handler;

import org.springframework.core.type.StandardMethodMetadata;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

/**
 * 方法描述信息
 *
 * @author bugcat
 * */
public class CatMethodInfo {

    /**
     * 原interface的方法
     * */
    private final StandardMethodMetadata interfaceMethod;

    /**
     * cglib生成的ctrl类方法
     * */
    private final Method controllerMethod;
    
    /**
     * 原server类的方法
     * */
    private final Method serverMethod;
    
    /**
     * controller快速调用server对象方法的代理类
     * */
    private final CatServiceMethodProxy serviceMethodProxy;

    /**
     * {@code @CatNote}注解信息
     * */
    private final Map<String, String> noteMap;

    /**
     * 方法上参数列表
     * */
    private final Map<String, Integer> paramIndex;
    
    
    protected CatMethodInfo(CatMethodInfoBuilder builder){
        this.interfaceMethod = builder.interfaceMethod;
        this.controllerMethod = builder.controllerMethod;
        this.serverMethod = builder.serverMethod;
        this.serviceMethodProxy = builder.serviceMethodProxy;
        this.noteMap = Collections.unmodifiableMap(builder.noteMap);
        this.paramIndex = Collections.unmodifiableMap(builder.paramIndex);
    }

    
    public StandardMethodMetadata getInterfaceMethod() {
        return interfaceMethod;
    }
    public Method getControllerMethod() {
        return controllerMethod;
    }
    public Method getServerMethod() {
        return serverMethod;
    }
    public CatServiceMethodProxy getServiceMethodProxy() {
        return serviceMethodProxy;
    }
    
    protected Map<String, Integer> getParamIndex() {
        return paramIndex;
    }
    protected Map<String, String> getNoteMap() {
        return noteMap;
    }
}
