package cc.bugcat.catserver.handler;

import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.beanInfos.CatServerInfo;
import cc.bugcat.catserver.handler.CatMethodInfo.ServiceMethodProxy;
import cc.bugcat.catserver.spi.*;
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
 * 通过cglib生成代理类
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
     * 响应结果处理类
     * */
    private final CatServerResultHandler resultHandler;

    /**
     * 方法信息
     * */
    private final CatMethodInfo methodInfo;


    protected CatMethodAopInterceptor(Builder builder) {
        this.serverInfo = builder.serverInfo;
        this.serverBean = builder.serverBean;
        this.argumentResolver = builder.argumentResolver;
        this.resultHandler = builder.resultHandler;
        this.methodInfo = builder.methodInfo;
    }


    @Override
    public Object intercept(Object controller, Method method, Object[] arguments, MethodProxy methodProxy) throws Throwable {

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        HttpServletResponse response = attr.getResponse();

        CatParameterResolver parameterResolver = methodInfo.getParameterResolver();
        StandardMethodMetadata interMethod = methodInfo.getInterMethod();
        Class<?> returnType = interMethod.getIntrospectedMethod().getReturnType();

        Object result = null;
        try {

            Object[] args = parameterResolver.resolveArguments(methodInfo, arguments);
            args = argumentResolver.resolveArguments(methodInfo, args);

            CatInterceptPoint interceptPoint = CatInterceptPoint.builder()
                    .request(request)
                    .response(response)
                    .serverInfo(serverInfo)
                    .target(serverBean)
                    .interMethod(interMethod)
                    .arguments(args)
                    .build();


            CatServerResultHandler resultHandler = this.resultHandler;

            /**
             * 激活的拦截器
             * */
            List<CatInterceptor> actives = new ArrayList<>();
            actives.add(methodInfo.getGlobalInterceptor()); // 全局

            for ( CatServerInterceptor interceptor : methodInfo.getInterceptors() ) {

                if ( CatServerDefaults.DEFAULT_INTERCEPTOR == interceptor ) {
                    /**
                     * 把默认拦截器位置，替换成拦截器组
                     * */
                    for ( CatInterceptorGroup group : methodInfo.getInterceptorGroups() ) {
                        if ( group.matcher(interceptPoint) ) {
                            CatServerResultHandler handler = group.getResultHandler();
                            if ( handler != null ) {
                                resultHandler = handler;
                            }
                            List<CatInterceptor> interceptors = group.getInterceptors();
                            if( interceptors != null ){
                                actives.addAll(interceptors);
                            }
                            break; // 只匹配一个拦截器组
                        }
                    }

                } else if ( CatServerDefaults.OFF_INTERCEPTOR == interceptor ) {
                    /**
                     * 关闭所有拦截器，包含全局
                     * */
                    actives.clear();
                    break;

                } else {
                    /**
                     * CatServer上自定义拦截器
                     * */
                    if ( interceptor.preHandle(interceptPoint) ) {
                        actives.add(interceptor);
                    }
                }
            }

            /**
             * 实际调用CatServer方法
             * */
            CatInterceptor controllerMethod = new ControllerMethodInterceptor(serverBean, methodInfo.getServiceMethodProxy(), args);

            CatServerContextHolder contextHolder = CatServerContextHolder.builder()
                    .interceptPoint(interceptPoint)
                    .interceptors(actives)
                    .controllerMethod(controllerMethod)
                    .methodInfo(methodInfo)
                    .resultHandler(resultHandler)
                    .build();


            // 原CatServer类返回的数据
            Object invoke = contextHolder.executeRequest();
            result = resultHandler.onSuccess(invoke, returnType);

        } catch ( Exception ex ) {
            result = resultHandler.onError(CatToosUtil.getCause(ex), returnType);
        } finally {
            CatServerContextHolder.remove();
        }
        return result;
    }



    private static final class ControllerMethodInterceptor implements CatInterceptor {

        private final Object serverBean;
        private final ServiceMethodProxy serviceMethodProxy;
        private final Object[] args;

        private ControllerMethodInterceptor(Object serverBean, ServiceMethodProxy serviceMethodProxy, Object[] args) {
            this.serverBean = serverBean;
            this.serviceMethodProxy = serviceMethodProxy;
            this.args = args;
        }

        @Override
        public Object postHandle(CatServerContextHolder contextHolder) throws Exception {
            try {
                return serviceMethodProxy.invokeProxy(serverBean, args);
            } catch ( Exception ex ) {
                throw ex;
            }
        }
    }




    protected static Builder builder(){
        return new Builder();
    }


    protected static class Builder {

        private CatServerInfo serverInfo;
        private Object serverBean;
        private CatParameterResolver argumentResolver;
        private CatServerResultHandler resultHandler;
        private CatMethodInfo methodInfo;

        public Builder serverInfo(CatServerInfo serverInfo) {
            this.serverInfo = serverInfo;
            return this;
        }

        public Builder serverBean(Object serverBean) {
            this.serverBean = serverBean;
            return this;
        }

        public Builder argumentResolver(CatParameterResolver argumentResolver) {
            this.argumentResolver = argumentResolver;
            return this;
        }

        public Builder resultHandler(CatServerResultHandler resultHandler) {
            this.resultHandler = resultHandler;
            return this;
        }

        public Builder methodInfo(CatMethodInfo methodInfo) {
            this.methodInfo = methodInfo;
            return this;
        }

        public CatMethodAopInterceptor build(){
            return new CatMethodAopInterceptor(this);
        }
    }


}
