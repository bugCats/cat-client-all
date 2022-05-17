package cc.bugcat.catclient.spi;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.config.CatClientConfiguration;

import java.util.function.Supplier;


/**
 * 发送http辅助对象工厂，在{@link CatClient#factory()}指定其class。
 * 
 * 一般如果仅需修改入参、或者添加签名等，可以使用{@link CatMethodSendInterceptor}。
 * 
 * 如果需要修改http整体流程，才需要考虑修改。
 * 
 * @see DefaultCatClientFactory
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
     * 得到日志处理对象
     * */
    CatLoggerProcessor getLoggerProcessor();

    /**
     * 得到结果处理对象
     * */
    CatResultProcessor getResultHandler();

    /**
     * 创建一个新的http发送对象
     * 必须为多例
     * */
    Supplier<CatSendProcessor> newSendHandler();

}
