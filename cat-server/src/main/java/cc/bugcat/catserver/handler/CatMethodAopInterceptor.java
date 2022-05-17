package cc.bugcat.catserver.handler;

import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.annotation.CatBefore;
import cc.bugcat.catserver.beanInfos.CatServerInfo;
import cc.bugcat.catserver.spi.*;
import cc.bugcat.catserver.utils.CatServerUtil;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 通过cglib生成代理类，
 * 
 * 将访问controller的事件，转发到CatServer类。
 *
 * @author bugcat
 */
public final class CatMethodAopInterceptor implements MethodInterceptor{

    /**
     * 标记的@CatServer注解信息
     * */
    private final CatServerInfo serverInfo;

    /**
     * 被@CatServer标记的server对象
     * */
    private final Object serverBean;

    /**
     * 自定义CatServer方法参数预处理
     * */
    private final CatParameterResolver argumentResolver;

    /**
     * 方法信息
     * */
    private final CatMethodInfo methodInfo;


    private CatMethodAopInterceptor(Builder builder) {
        this.serverInfo = builder.serverInfo;
        this.serverBean = builder.serverBean;
        this.argumentResolver = builder.argumentResolver;
        this.methodInfo = builder.methodInfo;
    }


    @Override
    public Object intercept(Object controller, Method method, Object[] arguments, MethodProxy methodProxy) throws Throwable {

        ServletRequestAttributes servletAttr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletAttr.getRequest();
        HttpServletResponse response = servletAttr.getResponse();

        // 精简模式下参数预处理器，如果非精简模式，则为默认值
        CatParameterResolver parameterResolver = methodInfo.getParameterResolver();
        
        // 原interface上方法信息
        StandardMethodMetadata interMethod = methodInfo.getInterMethod();

        // 结果处理类
        CatResultHandler resultHandler = methodInfo.getResultHandler();
        
        // 原interface返回对象
        Class<?> returnType = interMethod.getIntrospectedMethod().getReturnType();
        
        Object result = null;
        try {

            Object[] args = parameterResolver.resolveArguments(methodInfo, arguments);
            
            // 自定义参数处理器
            args = argumentResolver.resolveArguments(methodInfo, args);

            CatInterceptPoint interceptPoint = CatInterceptPoint.builder()
                    .request(request)
                    .response(response)
                    .serverInfo(serverInfo)
                    .target(serverBean)
                    .interMethod(interMethod)
                    .arguments(args)
                    .build();
            
            

            // 激活的拦截器
            List<CatServerInterceptor> actives = new ArrayList<>();

            for ( CatServerInterceptor interceptor : methodInfo.getInterceptors() ) {

                // 拦截器组占位，替换成匹配全局拦截器组
                if ( CatServerDefaults.GROUP_INTERCEPTOR == interceptor ) { 
                    for ( CatInterceptorGroup group : methodInfo.getInterceptorGroups() ) {
                        if ( group.matcher(interceptPoint) ) {
                            List<CatServerInterceptor> interceptors = group.getInterceptorFactory().get();
                            if( interceptors != null ){
                                for ( CatServerInterceptor groupInterceptor : interceptors ) {
                                    if ( groupInterceptor.preHandle(interceptPoint) ) {
                                        actives.add(groupInterceptor);
                                    }
                                }
                            }
                            break; // 只匹配一个拦截器组
                        }
                    }
                    
                } else if ( CatServerDefaults.OFF_INTERCEPTOR == interceptor ) {
                    // 关闭所有拦截器，包含全局拦截器组
                    actives.clear();
                    break;
                    
                } else { 
                    // CatServer上自定义拦截器，如果满足，则放入拦截器链中
                    if ( interceptor.preHandle(interceptPoint) ) {
                        actives.add(interceptor);
                    }
                }
            }

            // 实际调用CatServer方法
            ControllerMethodInterceptor controllerMethod = new ControllerMethodInterceptor(serverBean, methodInfo.getServiceMethodProxy(), args);

            CatServerContextHolder contextHolder = CatServerContextHolder.builder()
                    .interceptPoint(interceptPoint)
                    .interceptors(actives)
                    .controllerMethod(controllerMethod)
                    .methodInfo(methodInfo)
                    .resultHandler(resultHandler)
                    .build();


            // 原CatServer类返回的数据
            Object invoke = contextHolder.proceedRequest();
            
            // 包装器类处理
            result = resultHandler.onSuccess(invoke, returnType);

        } catch ( Exception ex ) {
            result = resultHandler.onError(CatToosUtil.getCause(ex), returnType);
        } finally {
            CatServerContextHolder.remove();
        }
        return result;
    }
    
    
    
    public boolean equalsMethod (Method method){
        return methodInfo.getControllerMethod().equals(method);
    }

    public CatMethodInfo getMethodInfo() {
        return methodInfo;
    }

    
    
    
    /**
     * 最后一级拦截器执行完毕之后，执行真正的CatServer类方法
     * */
    protected static final class ControllerMethodInterceptor  {

        private final Object serverBean;
        private final CatServiceMethodProxy serviceMethodProxy;
        private final Object[] args;

        private ControllerMethodInterceptor(Object serverBean, CatServiceMethodProxy serviceMethodProxy, Object[] args) {
            this.serverBean = serverBean;
            this.serviceMethodProxy = serviceMethodProxy;
            this.args = args;
        }

        protected Object invoke() throws Exception {
            try {
                return serviceMethodProxy.invokeProxy(serverBean, args);
            } catch ( Exception ex ) {
                throw ex;
            }
        }
    }

    
    
    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {

        private CatServerInfo serverInfo;
        private Object serverBean;
        private CatParameterResolver argumentResolver;
        private CatMethodInfo methodInfo;

        public Builder serverInfo(CatServerInfo serverInfo) {
            this.serverInfo = serverInfo;
            return this;
        }

        public Builder serverBean(Object serverBean) {
            this.serverBean = serverBean;
            return this;
        }
        
        public Builder methodInfo(CatMethodInfo methodInfo) {
            this.methodInfo = methodInfo;
            return this;
        }

        public CatMethodAopInterceptor build(){
            CatBefore catBefore = methodInfo.getServerMethod().getAnnotation(CatBefore.class);
            if( catBefore != null ){
                Class<? extends CatParameterResolver> resolverClass = catBefore.value();
                argumentResolver = CatServerUtil.getBean(resolverClass);
            } else {
                argumentResolver = CatServerDefaults.DEFAULT_RESOLVER;
            }
            return new CatMethodAopInterceptor(this);
        }
    }


}
