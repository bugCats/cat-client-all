package cc.bugcat.catserver.handler;

import cc.bugcat.catserver.beanInfos.CatServerInfo;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.spi.CatInterceptorGroup;
import cc.bugcat.catserver.spi.CatParameterResolver;
import cc.bugcat.catserver.spi.CatResultHandler;
import cc.bugcat.catserver.spi.CatServerInterceptor;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 方法描述信息
 *
 * @author bugcat
 * */
public class CatMethodInfo {

    /**
     * 原interface的方法
     * */
    private final StandardMethodMetadata interMethod;

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
     * 运行时拦截器组
     * @see CatServerConfiguration#getInterceptorGroup()
     * */
    private final List<CatServerInterceptor> interceptors;

    /**
     * 拦截器组。
     * 运行时可动态添加移除的拦截器
     * */
    private final List<CatInterceptorGroup> interceptorGroups;

    /**
     * 配置的结果处理类
     * */
    private final CatResultHandler resultHandler;
    
    /**
     * 精简模式下参数预处理器
     * */
    private final CatParameterResolver parameterResolver;


    protected CatMethodInfo(CatMethodInfoBuilder builder){
        this.interMethod = builder.interMethod;
        this.controllerMethod = builder.controllerMethod;
        this.serverMethod = builder.serverMethod;
        this.serviceMethodProxy = builder.serviceMethodProxy;
        this.interceptors = builder.interceptors;
        this.resultHandler = builder.resultHandler;
        this.interceptorGroups = builder.interceptorGroups;
        this.parameterResolver = builder.parameterResolver;
    }

    
    public StandardMethodMetadata getInterMethod() {
        return interMethod;
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
    public List<CatServerInterceptor> getInterceptors() {
        return interceptors;
    }
    public List<CatInterceptorGroup> getInterceptorGroups() {
        return interceptorGroups;
    }
    public CatResultHandler getResultHandler() {
        return resultHandler;
    }
    public CatParameterResolver getParameterResolver() {
        return parameterResolver;
    }
    

}
