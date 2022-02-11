package cc.bugcat.catclient.handler;


import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.spi.*;


/**
 * 默认工厂，设置请求发送类、结果响应类
 *
 * @author bugcat
 * */
public class DefaultCatClientFactory implements CatClientFactory {


    private CatClientConfiguration configuration;


    /**
     * 全局配置对象
     * */
    @Override
    public void setClientConfiguration(CatClientConfiguration clientConfiguration) {
        this.configuration = clientConfiguration;
    }

    /**
     * http发送对象
     * */
    @Override
    public CatHttp getCatHttp() {
        return configuration.catHttp();
    }

    /**
     * 对象序列化与反序列化
     * */
    @Override
    public CatJsonResolver getJsonResolver() {
        return configuration.jsonResolver();
    }

    /**
     * 发送类对象
     * */
    @Override
    public CatSendProcessor newSendHandler() {
        return new DefaultSendHandler();
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
        return new DefaultResultHandler();
    }


}