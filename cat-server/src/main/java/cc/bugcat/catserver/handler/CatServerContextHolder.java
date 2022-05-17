package cc.bugcat.catserver.handler;

import cc.bugcat.catserver.spi.CatServerInterceptor;
import cc.bugcat.catserver.spi.CatResultHandler;
import cc.bugcat.catserver.handler.CatMethodAopInterceptor.ControllerMethodInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;


/**
 * 拦截器控制器
 * @author bugcat
 * */
public class CatServerContextHolder {



    private static ThreadLocal<CatServerContextHolder> threadLocal = new ThreadLocal<>();

    /**
     * 在同一个线程中可以获取
     * */
    public static CatServerContextHolder getContextHolder() {
        return threadLocal.get();
    }

    protected static void remove() {
        threadLocal.remove();
    }

    /**
     * controller实例、CatServer实例、入参信息等
     * */
    private final CatInterceptPoint interceptPoint;
    
    /**
     * 有效的拦截器组
     * */
    private final List<? extends CatServerInterceptor> interceptors;
    
    /**
     * 执行CatServer类的拦截器
     * */
    private final ControllerMethodInterceptor controllerMethod;
    
    /**
     * 方法信息
     * */
    private final CatMethodInfo methodInfo;
    
    
    /**
     * 结果处理器
     * */
    private final CatResultHandler resultHandler;

    /**
     * 所有的有效拦截器数量
     * */
    private final int interceptorCount;

    /**
     * 当前拦截器索引
     * */
    private int interceptorIndex = 0;



    private CatServerContextHolder(Builder builder){
        this.interceptPoint = builder.interceptPoint;
        this.interceptors = builder.interceptors;
        this.controllerMethod = builder.controllerMethod;
        this.methodInfo = builder.methodInfo;
        this.resultHandler = builder.resultHandler;
        this.interceptorCount = interceptors.size();
    }



    public CatInterceptPoint getInterceptPoint() {
        return interceptPoint;
    }

    public HttpServletRequest getRequest() {
        return interceptPoint.getRequest();
    }

    public HttpServletResponse getResponse() {
        return interceptPoint.getResponse();
    }


    /**
     * 执行
     * */
    public Object proceedRequest() throws Throwable {
        if ( interceptorIndex < interceptorCount ){
            try {
                CatServerInterceptor interceptor = interceptors.get(interceptorIndex ++ );
                return interceptor.postHandle(this);
            } catch ( Throwable ex ) {
                throw ex;
            }
        }
        return controllerMethod.invoke();
    }


    /**
     * 当发生异常时
     * */
    public Object onErrorToWrapper(Throwable throwable) {
        try {
            Method method = methodInfo.getInterMethod().getIntrospectedMethod();
            Class<?> returnType = method.getReturnType();
            return resultHandler.onError(throwable, returnType);
        } catch ( Throwable ex ) {
            throw new RuntimeException(ex);
        }
    }


    protected static Builder builder(){
        return new Builder();
    }


    protected static class Builder {

        private CatInterceptPoint interceptPoint;
        private List<? extends CatServerInterceptor> interceptors;
        private ControllerMethodInterceptor controllerMethod;
        private CatMethodInfo methodInfo;
        private CatResultHandler resultHandler;

        public Builder interceptPoint(CatInterceptPoint interceptPoint) {
            this.interceptPoint = interceptPoint;
            return this;
        }

        public Builder interceptors(List<? extends CatServerInterceptor> interceptors) {
            this.interceptors = interceptors;
            return this;
        }

        public Builder controllerMethod(ControllerMethodInterceptor controllerMethod) {
            this.controllerMethod = controllerMethod;
            return this;
        }

        public Builder methodInfo(CatMethodInfo methodInfo) {
            this.methodInfo = methodInfo;
            return this;
        }

        public Builder resultHandler(CatResultHandler resultHandler) {
            this.resultHandler = resultHandler;
            return this;
        }

        protected CatServerContextHolder build(){
            CatServerContextHolder contextHolder = new CatServerContextHolder(this);
            threadLocal.set(contextHolder);
            return contextHolder;
        }
    }

}
