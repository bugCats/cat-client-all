package cc.bugcat.catclient.spi;

import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.handler.CatResultProcessor;
import cc.bugcat.catclient.handler.CatSendProcessor;


/**
 * 发送http辅助对象工厂
 *
 * @author bugcat
 * */
public interface CatClientFactory {

    /**
     * 设置全局配置
     * */
    void setClientConfiguration(CatClientConfiguration clientConfiguration);

    /**
     * 得到http请求对象
     * */
    CatHttp getCatHttp();

    /**
     * 得到对象序列化与反序列化对象
     * */
    CatJsonResolver getJsonResolver();

    /**
     * 创建一个新的http发送对象
     * */
    CatSendProcessor newSendHandler();

    /**
     * 得到日志处理对象
     * */
    CatLoggerProcessor getLoggerProcessor();

    /**
     * 得到结果处理对象
     * */
    CatResultProcessor getResultHandler();

}
