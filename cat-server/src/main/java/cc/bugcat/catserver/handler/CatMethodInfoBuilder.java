package cc.bugcat.catserver.handler;

import cc.bugcat.catserver.beanInfos.CatServerInfo;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.spi.*;
import cc.bugcat.catserver.utils.CatServerUtil;
import org.springframework.asm.Type;
import org.springframework.cglib.core.Signature;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author bugcat
 * */
public final class CatMethodInfoBuilder {


    /**
     * 被@CatServer标记的server对象实例
     * */
    private final Object serverBean;

    /**
     * serverBean的class。
     * 如果server对象被其他组件动态代理，则为代理后的class！
     * serverBeanClass 不一定等于 serverClass
     * */
    private final Class serverBeanClass;
    
    /**
     * cglib 的动态调用类
     * */
    private FastClass fastClass;

    /**
     * {@code @CatServer}配置的拦截器
     * */
    protected List<CatServerInterceptor> interceptors;
    
    /**
     * 配置的结果处理类
     * */
    protected CatResultHandler resultHandler;
    
    /**
     * 运行时拦截器组
     * @see CatServerConfiguration#getInterceptorGroup()
     * */
    protected List<CatInterceptorGroup> interceptorGroups;
    
    
    private CatMethodInfoBuilder(Class serverClass, CatServerInfo serverInfo){
        this.serverBean = CatServerUtil.getBean(serverClass);
        this.serverBeanClass = serverBean.getClass();
        if( !ClassUtils.isCglibProxy(serverBeanClass) ){// server对象，没有、或者不是cglib代理，使用快速处理类
            this.fastClass = FastClass.create(serverBeanClass);
        }
        this.parseInterceptor(serverInfo);
        this.parseResultHandler(serverInfo);
    }
    

    
    public static CatMethodInfoBuilder builder(Class serverClass, CatServerInfo serverInfo){
        return new CatMethodInfoBuilder(serverClass, serverInfo);
    }

    
    /**
     * 获取拦截器
     * */
    private void parseInterceptor(CatServerInfo serverInfo) {

        CatServerConfiguration serverConfig = serverInfo.getServerConfig();
        List<CatInterceptorGroup> interceptorGroup = serverConfig.getInterceptorGroup();
        Set<Class<? extends CatServerInterceptor>> interceptors = serverInfo.getInterceptors();
        
        List<CatServerInterceptor> handers = new ArrayList<>(interceptors.size());
        for ( Class<? extends CatServerInterceptor> clazz : interceptors ) {
            if ( CatServerInterceptor.Off.class.equals(clazz) ) {
                // 关闭所有拦截器
                handers.clear();
                break;

            } else if (CatServerInterceptor.class.equals(clazz) ) {
                // 默认拦截器，使用CatServerConfiguration.getGlobalInterceptor()替换
                handers.add(serverConfig.getDefaultInterceptor());

            } else {
                // CatServer上自定义拦截器
                handers.add(CatServerUtil.getBean(clazz));
            }
        }
        
        Collections.sort(interceptorGroup, (i1, i2) -> i1.getOrder() < i2.getOrder() ? 1 : -1);
        
        this.interceptors = handers;
        this.interceptorGroups = interceptorGroup;
    }

    
    /**
     * 响应处理器
     * */
    private void parseResultHandler(CatServerInfo serverInfo) {
        this.resultHandler = CatServerUtil.getBean(serverInfo.getResultHandler());
        this.resultHandler.setResponesWrapper(serverInfo.getWrapperHandler());
    }

    
    /**
     * 原interface的方法
     * */
    protected StandardMethodMetadata interMethod;

    /**
     * cglib生成的ctrl类方法
     * */
    protected Method controllerMethod;
    
    
    /**
     * 原server类的方法
     * */
    protected Method serverMethod;

    /**
     * controller快速调用server对象方法的
     * */
    protected CatServiceMethodProxy serviceMethodProxy;

    /**
     * 精简模式下参数预处理器
     * */
    protected CatParameterResolver parameterResolver;


    /**
     * 原interface的方法
     * */
    public CatMethodInfoBuilder interMethod(StandardMethodMetadata interMethod) {
        this.interMethod = interMethod;
        
        Method method = interMethod.getIntrospectedMethod();
        if( fastClass != null ){
            // server对象，没有、或者不是cglib代理，使用快速处理类
            FastMethod fastMethod = fastClass.getMethod(method);
            this.serviceMethodProxy = CatServiceMethodProxy.getFastProxy(fastMethod);
        } else {
            // server对象，被cglib代理
            MethodProxy proxy = MethodProxy.find(serverBeanClass, new Signature(method.getName(), Type.getMethodDescriptor(method)));
            this.serviceMethodProxy = CatServiceMethodProxy.getCglibProxy(proxy);
        }
        return this;
    }
    
    /**
     * cglib生成的ctrl类方法
     * */

    public CatMethodInfoBuilder controllerMethod(Method controllerMethod) {
        this.controllerMethod = controllerMethod;
        return this;
    }
    
    /**
     * server类的方法
     * */
    public CatMethodInfoBuilder serverMethod(Method serverMethod) {
        this.serverMethod = serverMethod;
        return this;
    }
    
    /**
     * 方法入参处理器
     * */
    public CatMethodInfoBuilder parameterResolver(CatParameterResolver parameterResolver) {
        this.parameterResolver = parameterResolver;
        return this;
    }

    
    /**
     * 生成方法描述信息对象，之后把临时缓存清空
     * */
    public CatMethodInfo build(){
        CatMethodInfo info = new CatMethodInfo(this);
        this.interMethod = null;
        this.serverMethod = null;
        this.serviceMethodProxy = null;
        this.parameterResolver = null;
        return info;
    }

    
    public Object getServerBean() {
        return serverBean;
    }
}
