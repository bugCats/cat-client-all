package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.beanInfos.CatMethodInfo;
import cc.bugcat.catclient.beanInfos.CatParameter;
import cc.bugcat.catclient.config.CatHttpRetryConfigurer;
import cc.bugcat.catclient.spi.CatResultProcessor;
import cc.bugcat.catclient.spi.CatSendInterceptors;
import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.catface.utils.CatToosUtil;

import java.util.List;

/**
 * http调用环境对象
 *
 * 在同一个线程中，可以通过{@link CatClientContextHolder#getContextHolder()}获取到当前环境参数
 *
 * 一般用于异常回调模式中，获取原始http异常信息
 *
 * @author bugcat
 * */
public class CatClientContextHolder {

    private static ThreadLocal<CatClientContextHolder> threadLocal = new ThreadLocal<>();

    /**
     * 在同一个线程中可以获取
     * */
    public static CatClientContextHolder getContextHolder() {
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
     * 最终方法返回的对象
     * 如果发生异常，可以在异常回调模式中，返回默认结果；
     * 或者在{@link CatResultProcessor#onHttpError(CatClientContextHolder)}方法中赋默认值；
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

    
    
    private final CatSendProcessor sendHandler;
    private final CatClientInfo clientInfo;
    private final CatMethodInfo methodInfo;
    private final CatClientFactoryAdapter factoryAdapter;
    private final CatHttpRetryConfigurer retryConfigurer;
    private final CatSendInterceptors interceptor;
    
    protected CatClientContextHolder(CatClientContextHolderBuilder builder) {
        this.sendHandler = builder.sendHandler;
        this.clientInfo = builder.clientInfo;
        this.methodInfo = builder.methodInfo;
        this.interceptor = builder.interceptor;
        this.factoryAdapter = builder.factoryAdapter;
        this.retryConfigurer = builder.retryConfigurer;
        threadLocal.set(this);
    }


    /**
     * 1、设置参数
     * */
    protected void executeConfigurationResolver(CatParameter parameter) {
        interceptor.executeConfigurationResolver(this, parameter, () -> {
            CatSendProcessor sendHandler = this.getSendHandler();
            sendHandler.doConfigurationResolver(this, parameter);
        });
    }
    
    /**
     * 2、http参数处理切入点
     * */
    protected void executeVariableResolver(){
        interceptor.executeVariableResolver(this, () -> {
            CatSendProcessor sendHandler = this.getSendHandler();
            sendHandler.doVariableResolver(this);
            sendHandler.postVariableResolver(this);
        });
    }
    
    /**
     * 3、http请求切入点
     * */
    protected String executeRequest() throws CatHttpException {
        this.responseBody = interceptor.executeHttpSend(sendHandler, () -> sendHandler.postHttpSend());
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

    protected static CatClientContextHolderBuilder builder(){
        return new CatClientContextHolderBuilder();
    }


    protected static class CatClientContextHolderBuilder {
        private CatClientInfo clientInfo;
        private CatMethodInfo methodInfo;
        private CatSendInterceptors interceptor;
        private CatSendProcessor sendHandler;
        private CatClientFactoryAdapter factoryAdapter;
        private CatHttpRetryConfigurer retryConfigurer;

        public CatClientContextHolderBuilder clientInfo(CatClientInfo clientInfo) {
            this.clientInfo = clientInfo;
            return this;
        }

        public CatClientContextHolderBuilder methodInfo(CatMethodInfo methodInfo) {
            this.methodInfo = methodInfo;
            return this;
        }

        public CatClientContextHolderBuilder interceptor(CatSendInterceptors interceptor) {
            this.interceptor = interceptor;
            return this;
        }

        public CatClientContextHolderBuilder sendHandler(CatSendProcessor sendHandler) {
            this.sendHandler = sendHandler;
            return this;
        }

        public CatClientContextHolderBuilder factoryAdapter(CatClientFactoryAdapter factoryAdapter) {
            this.factoryAdapter = factoryAdapter;
            return this;
        }

        public CatClientContextHolderBuilder retryConfigurer(CatHttpRetryConfigurer retryConfigurer) {
            this.retryConfigurer = retryConfigurer;
            return this;
        }

        public CatClientContextHolder build(){
            CatClientContextHolder holder = new CatClientContextHolder(this);
            return holder;
        }
    }

}