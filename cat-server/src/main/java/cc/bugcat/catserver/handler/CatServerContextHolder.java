package cc.bugcat.catserver.handler;

import cc.bugcat.catserver.spi.CatInterceptor;
import cc.bugcat.catserver.spi.CatServerResultHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class CatServerContextHolder {



    private static ThreadLocal<CatServerContextHolder> threadLocal = new ThreadLocal<>();

    /**
     * 在同一个线程中可以获取
     * */
    public static CatServerContextHolder getContextHolder() {
        return threadLocal.get();
    }


    private final CatInterceptPoint interceptPoint;
    private final List<? extends CatInterceptor> interceptors;
    private final CatInterceptor controllerMethod;
    private final CatServerResultHandler resultHandler;


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
        this.resultHandler = builder.resultHandler;
        this.interceptorCount = interceptors.size();
    }


    protected void remove() {
        threadLocal.remove();
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
    public Object executeRequest() throws Exception {
        if ( interceptorIndex < interceptorCount ){
            try {
                CatInterceptor interceptor = interceptors.get(interceptorIndex ++ );
                return interceptor.postHandle(this);
            } catch ( Exception ex ) {
                throw ex;
            }
        }
        return controllerMethod.postHandle(this);
    }


    /**
     * 当发生异常时
     * */
    public Object onErrorToWrapper(Throwable exception) {
        try {
            return resultHandler.onError(exception);
        } catch ( Throwable ex ) {
            throw new RuntimeException(ex);
        }
    }



    protected static Builder builder(){
        return new Builder();
    }


    protected static class Builder {

        private CatInterceptPoint interceptPoint;
        private List<? extends CatInterceptor> interceptors;
        private CatInterceptor controllerMethod;
        private CatServerResultHandler resultHandler;

        public Builder interceptPoint(CatInterceptPoint interceptPoint) {
            this.interceptPoint = interceptPoint;
            return this;
        }

        public Builder interceptors(List<? extends CatInterceptor> interceptors) {
            this.interceptors = interceptors;
            return this;
        }

        public Builder controllerMethod(CatInterceptor controllerMethod) {
            this.controllerMethod = controllerMethod;
            return this;
        }

        public Builder resultHandler(CatServerResultHandler resultHandler) {
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
