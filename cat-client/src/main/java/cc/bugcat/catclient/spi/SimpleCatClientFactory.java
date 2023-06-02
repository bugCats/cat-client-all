package cc.bugcat.catclient.spi;


import cc.bugcat.catclient.config.CatClientConfiguration;

import java.util.function.Supplier;


/**
 * 默认工厂，设置请求发送类、结果响应类
 *
 * @author bugcat
 * */
public class SimpleCatClientFactory implements CatClientFactory {


    private CatClientConfiguration configuration;
    private CatResultProcessor simpleResultHandler;

    /**
     * 全局配置对象
     * */
    @Override
    public void setClientConfiguration(CatClientConfiguration clientConfiguration) {
        this.configuration = clientConfiguration;
        this.simpleResultHandler = new SimpleResultHandler();
    }

    /**
     * http发送对象
     * */
    @Override
    public CatHttp getCatHttp() {
        return configuration.getCatHttp();
    }

    /**
     * 对象序列化与反序列化
     * */
    @Override
    public CatPayloadResolver getPayloadResolver() {
        return configuration.getPayloadResolver();
    }

    /**
     * 日志处理类对象
     * */
    @Override
    public CatLoggerProcessor getLoggerProcessor() {
        return configuration.getLoggerProcessor();
    }


    /**
     * 结果处理类对象
     * */
    @Override
    public CatResultProcessor getResultHandler() {
        return simpleResultHandler;
    }

    /**
     * 发送类对象
     * 必须为多例
     * */
    @Override
    public Supplier<CatSendProcessor> newSendHandler() {
        return () -> new SimpleSendHandler();
    }

}
