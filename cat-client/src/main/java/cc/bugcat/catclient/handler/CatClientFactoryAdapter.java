package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.spi.*;

import java.util.function.Supplier;


/**
 * CatClient工厂类适配器
 * @author bugcat
 * */
public class CatClientFactoryAdapter {

    private final CatLoggerProcessor loggerProcessor;
    private final CatResultProcessor resultHandler;
    private final CatJsonResolver jsonResolver;
    private final CatHttp catHttp;
    private final Supplier<CatSendProcessor> sendProcessorSupplier;

    public CatClientFactoryAdapter(CatClientFactory bridge) {
        this.resultHandler = bridge.getResultHandler();
        this.catHttp = bridge.getCatHttp();
        this.loggerProcessor = bridge.getLoggerProcessor();
        this.jsonResolver = bridge.getJsonResolver();
        this.sendProcessorSupplier = bridge.newSendHandler();
    }


    /**
     * http 类
     */
    public CatHttp getCatHttp() {
        return this.catHttp;
    }

    /**
     * 如果在定义请求方法时，没有传入请求发送类，则在代理类中，自动生成一个请求发送类对象
     */
    public CatSendProcessor newSendHandler() {
        return sendProcessorSupplier.get();
    }

    /**
     * 日志处理器
     */
    public CatLoggerProcessor getLoggerProcessor() {
        return this.loggerProcessor;
    }

    /**
     * 获取结果处理类
     */
    public CatResultProcessor getResultHandler() {
        return this.resultHandler;
    }

    /**
     * 获取对象序列化处理类
     */
    public CatJsonResolver getJsonResolver() {
        return this.jsonResolver;
    }


}