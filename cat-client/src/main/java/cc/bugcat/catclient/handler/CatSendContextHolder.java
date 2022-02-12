package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.beanInfos.CatMethodInfo;
import cc.bugcat.catclient.beanInfos.CatParameter;
import cc.bugcat.catclient.config.CatHttpRetryConfigurer;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatMethodInterceptor;

import java.util.List;
import java.util.UUID;

/**
 * http调用环境对象
 *
 * 在同一个线程中，可以通过{@link CatSendContextHolder#getContextHolder()}获取到当前环境参数
 *
 * 一般用于异常回调模式中，获取原始http异常信息
 *
 * @author bugcat
 * */
public final class CatSendContextHolder {

    private static ThreadLocal<CatSendContextHolder> threadLocal = new ThreadLocal<>();

    /**
     * 在同一个线程中可以获取
     * */
    public static CatSendContextHolder getContextHolder() {
        return threadLocal.get();
    }



    /**
     * http原始响应内容，不一定有值
     * */
    private String responseBody;
    /**
     * http、或者对象反序列化异常时，一定不为null
     * */
    private Exception exception;
    /**
     * 如果发生异常，可以在异常回调模式中，返回默认结果
     * 或者在{@link CatResultProcessor#onHttpError(CatSendContextHolder)}方法中赋默认值
     * */
    private Object result;



    public String getResponseBody() {
        return responseBody;
    }

    public Exception getException() {
        return exception;
    }
    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Object getResult() {
        return result;
    }
    public void setResult(Object result) {
        this.result = result;
    }



    /**
     * 唯一标识
     * */
    private final String uuid;
    private final CatSendProcessor sendHandler;
    private final CatClientInfo clientInfo;
    private final CatMethodInfo methodInfo;
    private final CatClientFactory clientFactory;
    private final CatHttpRetryConfigurer retryConfigurer;
    private final CatMethodInterceptor interceptor;

    private CatSendContextHolder(CatSendContextHolderBuilder builder) {
        this.uuid = UUID.randomUUID().toString();
        this.sendHandler = builder.sendHandler;
        this.clientInfo = builder.clientInfo;
        this.methodInfo = builder.methodInfo;
        this.interceptor = builder.interceptor;
        this.clientFactory = builder.clientFactory;
        this.retryConfigurer = builder.retryConfigurer;
    }



    /**
     * http参数处理切入点
     * */
    protected void executeVariable(){
        interceptor.executeVariable(this, sendHandler);
    }

    /**
     * http请求切入点
     * */
    protected String executeRequest() throws CatHttpException {
        this.responseBody = interceptor.executeHttpSend(sendHandler);
        return responseBody;
    }


    /**
     * 打印http输入输出日志
     * */
    protected void printLog() {
        CatHttpPoint httpPoint = sendHandler.getHttpPoint();
        List<CatClientLogger> catLogs = httpPoint.getCatLogs();
        catLogs.forEach(catLog -> clientFactory.getLoggerProcessor().printLog(catLog));
    }


    protected void remove() {
        threadLocal.remove();
    }



    public String getUuid() {
        return uuid;
    }
    public CatSendProcessor getSendHandler() {
        return sendHandler;
    }
    public CatClientInfo getClientInfo() {
        return clientInfo;
    }
    public CatMethodInfo getMethodInfo() {
        return methodInfo;
    }
    public CatClientFactory getClientFactory() {
        return clientFactory;
    }
    public CatHttpRetryConfigurer getRetryConfigurer() {
        return retryConfigurer;
    }



    /****************************************************************************************************************/

    protected static CatSendContextHolderBuilder builder(){
        return new CatSendContextHolderBuilder();
    }

    protected static class CatSendContextHolderBuilder {
        private CatClientInfo clientInfo;
        private CatMethodInfo methodInfo;
        private CatMethodInterceptor interceptor;
        private CatSendProcessor sendHandler;
        private CatClientFactory clientFactory;
        private CatHttpRetryConfigurer retryConfigurer;

        public CatSendContextHolderBuilder clientInfo(CatClientInfo clientInfo) {
            this.clientInfo = clientInfo;
            return this;
        }

        public CatSendContextHolderBuilder methodInfo(CatMethodInfo methodInfo) {
            this.methodInfo = methodInfo;
            return this;
        }

        public CatSendContextHolderBuilder interceptor(CatMethodInterceptor interceptor) {
            this.interceptor = interceptor;
            return this;
        }

        public CatSendContextHolderBuilder sendHandler(CatSendProcessor sendHandler) {
            this.sendHandler = sendHandler;
            return this;
        }

        public CatSendContextHolderBuilder clientFactory(CatClientFactory clientFactory) {
            this.clientFactory = clientFactory;
            return this;
        }

        public CatSendContextHolderBuilder retryConfigurer(CatHttpRetryConfigurer retryConfigurer) {
            this.retryConfigurer = retryConfigurer;
            return this;
        }

        public CatSendContextHolder build(){
            CatSendContextHolder holder = new CatSendContextHolder(this);
            threadLocal.set(holder);
            return holder;
        }
    }

}
