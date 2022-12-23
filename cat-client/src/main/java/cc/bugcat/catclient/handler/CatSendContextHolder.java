package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.beanInfos.CatMethodInfo;
import cc.bugcat.catclient.beanInfos.CatParameter;
import cc.bugcat.catclient.config.CatHttpRetryConfigurer;
import cc.bugcat.catclient.spi.CatMethodSendInterceptor;
import cc.bugcat.catclient.spi.CatResultProcessor;
import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.catface.utils.CatToosUtil;

import java.util.List;
import java.util.UUID;

/**
 * http调用环境对象
 *
 * 在同一个线程中，可以通过{@link CatSendContextHolder#getContextHolder()}获取到当前环境参数
 *
 * 一般用于异常回调模式中，获取原始http异常信息
 *
 * 或者使用{@link CatToosUtil#getException()}获取原始http异常
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
    
    
    protected void remove() {
        threadLocal.remove();
    }


    /**
     * http原始响应内容，不一定有值
     * */
    private String responseBody;

    /**
     * 如果发生异常，可以在异常回调模式中，返回默认结果
     * 或者在{@link CatResultProcessor#onHttpError(CatSendContextHolder)}方法中赋默认值
     * */
    private Object result;

    /**
     * 进入异常流程时，存储异常信息
     * */
    private Throwable throwable;


    public String getResponseBody() {
        return responseBody;
    }

    public Object getResult() {
        return result;
    }
    public void setResult(Object result) {
        this.result = result;
    }

    
    public Throwable getException() {
        return this.throwable;
    }
    public void setException(Throwable error) {
        this.throwable = CatToosUtil.getCause(error);
    }

    /**
     * 唯一标识
     * */
    private final String uuid;
    private final CatSendProcessor sendHandler;
    private final CatClientInfo clientInfo;
    private final CatMethodInfo methodInfo;
    private final CatClientFactoryAdapter factoryAdapter;
    private final CatHttpRetryConfigurer retryConfigurer;
    private final CatMethodSendInterceptor interceptor;

    private CatSendContextHolder(CatSendContextHolderBuilder builder) {
        this.uuid = UUID.randomUUID().toString();
        this.sendHandler = builder.sendHandler;
        this.clientInfo = builder.clientInfo;
        this.methodInfo = builder.methodInfo;
        this.interceptor = builder.interceptor;
        this.factoryAdapter = builder.factoryAdapter;
        this.retryConfigurer = builder.retryConfigurer;
    }


    /**
     * 1、设置参数
     * */
    protected void executeConfigurationResolver(CatParameter parameter) {
        interceptor.executeConfigurationResolver(this, parameter);
    }
    
    /**
     * 2、http参数处理切入点
     * */
    protected void executeVariableResolver(){
        interceptor.executeVariableResolver(this);
    }
    
    /**
     * 3、http请求切入点
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
        catLogs.forEach(catLog -> factoryAdapter.getLoggerProcessor().printLog(catLog));
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
    public CatClientFactoryAdapter getFactoryAdapter() {
        return factoryAdapter;
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
        private CatMethodSendInterceptor interceptor;
        private CatSendProcessor sendHandler;
        private CatClientFactoryAdapter factoryAdapter;
        private CatHttpRetryConfigurer retryConfigurer;

        public CatSendContextHolderBuilder clientInfo(CatClientInfo clientInfo) {
            this.clientInfo = clientInfo;
            return this;
        }

        public CatSendContextHolderBuilder methodInfo(CatMethodInfo methodInfo) {
            this.methodInfo = methodInfo;
            return this;
        }

        public CatSendContextHolderBuilder interceptor(CatMethodSendInterceptor interceptor) {
            this.interceptor = interceptor;
            return this;
        }

        public CatSendContextHolderBuilder sendHandler(CatSendProcessor sendHandler) {
            this.sendHandler = sendHandler;
            return this;
        }

        public CatSendContextHolderBuilder factoryAdapter(CatClientFactoryAdapter factoryAdapter) {
            this.factoryAdapter = factoryAdapter;
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
