package cc.bugcat.catserver.handler;

import cc.bugcat.catserver.beanInfos.CatServerInfo;
import cc.bugcat.catserver.handler.CatMethodAopInterceptorBuilder.ServiceMethodProxy;
import cc.bugcat.catserver.spi.CatInterceptor;
import cc.bugcat.catserver.spi.CatServerInterceptor;
import cc.bugcat.catserver.spi.CatInterceptorGroup;
import cc.bugcat.catserver.spi.CatServerResultHandler;
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
 * 单例
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
     * 原interface的方法
     * */
    private final StandardMethodMetadata interMethod;

    /**
     * controller快递调用server对象方法的
     * */
    private final ServiceMethodProxy serviceMethodProxy;
    /**
     * 全局拦截器
     * */
    private final CatInterceptor globalInterceptor;
    /**
     * controller上的拦截器
     * */
    private final List<CatServerInterceptor> interceptors;
    /**
     * 运行时拦截器
     * */
    private final List<CatInterceptorGroup> interceptorGroups;
    /**
     * controller参数预处理器
     * */
    private final CatArgumentResolver argumentResolver;

    /**
     * 响应结果处理类
     * */
    private final CatServerResultHandler resultHandler;



    protected CatMethodAopInterceptor(CatMethodAopInterceptorBuilder builder) {
        this.serverInfo = builder.serverInfo;
        this.serverBean = builder.serverBean;
        this.serviceMethodProxy = builder.serviceMethodProxy;
        this.globalInterceptor = builder.globalInterceptor;
        this.interceptors = builder.interceptors;
        this.interceptorGroups = builder.interceptorGroups;
        this.interMethod = builder.interMethodMetadata;
        this.argumentResolver = builder.argumentResolver;
        this.resultHandler = builder.resultHandler;
    }


    @Override
    public Object intercept(Object controller, Method method, Object[] arguments, MethodProxy methodProxy) throws Throwable {

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        HttpServletResponse response = attr.getResponse();

        Object[] args = argumentResolver.resolveArguments(arguments);

        CatInterceptPoint interceptPoint = CatInterceptPoint.builder()
                .request(request)
                .response(response)
                .serverInfo(serverInfo)
                .target(serverBean)
                .interMethod(interMethod)
                .arguments(args)
                .build();


        /**
         * 激活的拦截器
         * */
        List<CatInterceptor> actives = new ArrayList<>();

        // 全局
        actives.add(globalInterceptor);


        CatServerResultHandler resultHandler = this.resultHandler;
        for ( CatServerInterceptor interceptor : interceptors ) {

            if ( CatServerInterceptor.DEFAULT == interceptor ) {
                /**
                 * 把默认拦截器位置，替换成拦截器组
                 * */
                for ( CatInterceptorGroup group : interceptorGroups ) {
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
            } else if ( CatServerInterceptor.OFF == interceptor ) {
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
        CatInterceptor controllerMethod = new ControllerMethodInterceptor(serverBean, serviceMethodProxy, args);

        CatServerContextHolder contextHolder = CatServerContextHolder.builder()
                .interceptPoint(interceptPoint)
                .interceptors(actives)
                .controllerMethod(controllerMethod)
                .resultHandler(resultHandler)
                .build();

        Object result = null;
        try {

            // 原CatServer类返回的数据
            Object invoke = contextHolder.executeRequest();
            result = resultHandler.onSuccess(invoke);

        } catch ( Exception ex ) {
            Throwable throwable = ex;
            while ( throwable.getCause() != null ) {
                throwable = throwable.getCause();
            }
            result = resultHandler.onError(throwable);
        } finally {
            contextHolder.remove();
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


}
