package cc.bugcat.catclient.spi;


import cc.bugcat.catclient.config.CatClientConfiguration;

import java.util.function.Supplier;


/**
 * 默认工厂，设置请求发送类、结果响应类
 *
 * @author bugcat
 * */
public class DefaultCatClientFactory implements CatClientFactory {


    private CatClientConfiguration configuration;
    private CatResultProcessor defaultResultHandler;

    /**
     * 全局配置对象
     * */
    @Override
    public void setClientConfiguration(CatClientConfiguration clientConfiguration) {
        this.configuration = clientConfiguration;
        this.defaultResultHandler = new DefaultResultHandler();
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
    public CatJsonResolver getJsonResolver() {
        return configuration.jsonResolver();
    }

    /**
     * 日志处理类对象
     * */
    @Override
    public CatLoggerProcessor getLoggerProcessor() {
        return configuration.loggerProcessor();
    }


    /**
     * 结果处理类对象
     * */
    @Override
    public CatResultProcessor getResultHandler() {
        return defaultResultHandler;
    }

    /**
     * 发送类对象
     * 必须为多例
     * */
    @Override
    public Supplier<CatSendProcessor> newSendHandler() {
        return () -> new DefaultSendHandler();
    }

}