package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatHttp;
import cc.bugcat.catclient.spi.CatJsonResolver;
import cc.bugcat.catclient.spi.CatLoggerProcessor;


/**
 * @author bugcat
 */
public class CatClientFactoryDecorator {


    private final CatClientFactory bridge;

    private final CatLoggerProcessor loggerProcessor;
    private final CatResultProcessor resultHandler;
    private final CatJsonResolver jsonResolver;
    private final CatHttp catHttp;

    public CatClientFactoryDecorator(CatClientFactory bridge) {
        this.bridge = bridge;
        this.resultHandler = bridge.getResultHandler();
        this.catHttp = bridge.getCatHttp().get();
        this.loggerProcessor = bridge.getLoggerProcessor().get();
        this.jsonResolver = bridge.getJsonResolver().get();
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
        return bridge.newSendHandler();
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
