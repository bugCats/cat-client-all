package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatHttp;
import cc.bugcat.catclient.spi.CatPayloadResolver;
import cc.bugcat.catclient.spi.CatLoggerProcessor;
import cc.bugcat.catclient.spi.CatResultProcessor;
import cc.bugcat.catclient.spi.CatSendProcessor;

import java.util.function.Supplier;


/**
 * CatClient工厂类适配器
 * 此处控制某些组件单例、与多例
 * @author bugcat
 * */
public class CatClientFactoryAdapter {

    private final CatLoggerProcessor loggerProcessor;
    private final CatResultProcessor resultHandler;
    private final CatPayloadResolver payloadResolver;
    private final CatHttp catHttp;
    private final Supplier<CatSendProcessor> sendProcessorSupplier;

    public CatClientFactoryAdapter(CatClientFactory bridge) {
        this.resultHandler = bridge.getResultHandler();
        this.catHttp = bridge.getCatHttp();
        this.loggerProcessor = bridge.getLoggerProcessor();
        this.payloadResolver = bridge.getPayloadResolver();
        this.sendProcessorSupplier = bridge.newSendHandler();
    }


    /**
     * http 类
     */
    public CatHttp getCatHttp() {
        return this.catHttp;
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
    public CatPayloadResolver getPayloadResolver() {
        return this.payloadResolver;
    }

    /**
     * 如果在定义请求方法时，没有传入请求发送类，则在代理类中，自动生成一个请求发送类对象
     */
    public CatSendProcessor newSendHandler() {
        return sendProcessorSupplier.get();
    }


}
