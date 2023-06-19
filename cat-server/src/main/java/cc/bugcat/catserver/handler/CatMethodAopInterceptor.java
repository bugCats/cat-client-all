package cc.bugcat.catserver.handler;

import cc.bugcat.catface.handler.CatContextHolder;
import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.annotation.CatBefore;
import cc.bugcat.catserver.spi.CatInterceptorGroup;
import cc.bugcat.catserver.spi.CatParameterResolver;
import cc.bugcat.catserver.spi.CatResultHandler;
import cc.bugcat.catserver.spi.CatServerInterceptor;
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
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * 通过cglib生成代理类，
 * 
 * 将访问controller的事件，转发到CatServer类。
 *
 * @author bugcat
 */
public final class CatMethodAopInterceptor implements MethodInterceptor{

    
    private final CatServerInfo serverInfo;
    
    private final CatMethodInfo methodInfo;

    /**
     * {@code CatServer}上自定义的拦截器和全局拦截器
     * */
    private final List<CatServerInterceptor> interceptors;

    /**
     * 拦截器组
     * */
    private final List<CatInterceptorGroup> interceptorGroups;
    
    /**
     * catface模式下参数预处理
     * */
    private final CatParameterResolver parameterResolver;

    /**
     * 结果处理
     * */
    private final CatResultHandler resultHandler;

    /**
     * 被{@code @CatServer}注解类的实例
     * */
    private final Object serverBean;

    /**
     * 自定义参数前置处理器
     * */
    private final CatParameterResolver argumentResolver;
    
    public CatMethodAopInterceptor(Builder builder) {
        this.serverInfo = builder.serverInfo;
        this.methodInfo = builder.methodInfo;
        this.interceptors = builder.interceptors;
        this.interceptorGroups = builder.interceptorGroups;
        this.parameterResolver = builder.parameterResolver;
        this.resultHandler = builder.resultHandler;
        this.serverBean = builder.serverBean;
        
        CatBefore catBefore = methodInfo.getServerMethod().getAnnotation(CatBefore.class);
        CatParameterResolver argumentResolver = null;
        if( catBefore != null ){
            Class<? extends CatParameterResolver> resolverClass = catBefore.value();
            argumentResolver = CatServerUtil.getBean(resolverClass);
        } else {
            argumentResolver = CatServerDepend.DEFAULT_RESOLVER;
        }
        this.argumentResolver = argumentResolver;
    }


    @Override
    public Object intercept(Object controller, Method method, Object[] arguments, MethodProxy methodProxy) throws Throwable {

        ServletRequestAttributes servletAttr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletAttr.getRequest();
        HttpServletResponse response = servletAttr.getResponse();

        // 原interface上方法信息
        StandardMethodMetadata interfaceMethod = methodInfo.getInterfaceMethod();

        // 原interface返回对象
        Class<?> methodReturnClass = interfaceMethod.getIntrospectedMethod().getReturnType();
        
        Object result = null;
        try {

            Object[] args = parameterResolver.resolveArguments(methodInfo, arguments);
            
            // 自定义参数处理器
            args = argumentResolver.resolveArguments(methodInfo, args);

            CatInterceptPoint interceptPoint = CatInterceptPoint.builder()
                    .request(request)
                    .response(response)
                    .serverInfo(serverInfo)
                    .methodInfo(methodInfo)
                    .target(serverBean)
                    .arguments(args)
                    .build();
            
            
            // 激活的拦截器
            List<CatServerInterceptor> actives = new ArrayList<>(interceptorGroups.size() + interceptors.size());
            //拦截器组，在自定义拦截器之前执行
            for ( CatInterceptorGroup group : interceptorGroups ) {
                if ( group.matcher(interceptPoint) ) {
                    Supplier<List<CatServerInterceptor>> supplier = group.getInterceptorFactory();
                    List<CatServerInterceptor> interceptors = null;
                    if( supplier != null && (interceptors = supplier.get()) != null ){
                        for ( CatServerInterceptor groupInterceptor : interceptors ) {
                            if ( groupInterceptor.preHandle(interceptPoint) ) {
                                actives.add(groupInterceptor);
                            }
                        }
                    }
                }
            }
            for ( CatServerInterceptor interceptor : interceptors) {
                if ( interceptor.preHandle(interceptPoint) ) { // CatServer上自定义拦截器，如果满足，则放入拦截器链中
                    actives.add(interceptor);
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

            //设置线程上下文
            CatContextHolder.setContext(contextHolder);
            

            // 原CatServer类返回的数据
            Object invoke = contextHolder.proceedRequest();
            
            // 包装器类处理
            result = resultHandler.onSuccess(invoke, methodReturnClass);

        } catch ( Throwable throwable ) {
            Throwable ex = CatToosUtil.getCause(throwable);
            CatContextHolder.setException(ex);
            result = resultHandler.onError(ex, methodReturnClass);
            
        } finally {
            CatContextHolder.remove();
        }
        return result;
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

        private CatMethodInfo methodInfo;
        
        private Object serverBean;
        
        private List<CatServerInterceptor> interceptors;

        private List<CatInterceptorGroup> interceptorGroups;

        private CatParameterResolver parameterResolver;

        private CatResultHandler resultHandler;


        public Builder serverInfo(CatServerInfo serverInfo) {
            this.serverInfo = serverInfo;
            return this;
        }
        
        public Builder methodInfo(CatMethodInfo methodInfo) {
            this.methodInfo = methodInfo;
            return this;
        }

        public Builder serverBean(Object serverBean) {
            this.serverBean = serverBean;
            return this;
        }

        public Builder interceptors(List<CatServerInterceptor> interceptors) {
            this.interceptors = interceptors;
            return this;
        }

        public Builder interceptorGroups(List<CatInterceptorGroup> interceptorGroups) {
            this.interceptorGroups = interceptorGroups;
            return this;
        }

        public Builder parameterResolver(CatParameterResolver parameterResolver) {
            this.parameterResolver = parameterResolver;
            return this;
        }

        public Builder resultHandler(CatResultHandler resultHandler) {
            this.resultHandler = resultHandler;
            return this;
        }


        public CatMethodAopInterceptor build(){
            if( interceptors == null ){
                interceptors = Collections.EMPTY_LIST;
            }
            if( interceptorGroups == null ){
                interceptorGroups = Collections.EMPTY_LIST;
            }
            return new CatMethodAopInterceptor(this);
        }
    }
}
