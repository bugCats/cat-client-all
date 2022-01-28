package cc.bugcat.catclient.spi;

import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.handler.AbstractCatResultProcessor;
import cc.bugcat.catclient.handler.CatSendProcessor;
import cc.bugcat.catclient.handler.DefaultCatClientFactory;

public interface CatClientFactory {

    void setClientConfiguration(CatClientConfiguration clientConfiguration);

    CatHttp getCatHttp();

    CatJsonResolver getJsonResolver();

    CatSendProcessor newSendHandler();

    CatLoggerProcessor getLoggerProcessor();

    AbstractCatResultProcessor getResultHandler();



    public static CatClientFactory defaultFactory(){
        return new DefaultCatClientFactory();
    }

    public static CatClientFactory factoryHandler(CatClientFactory factory){
        return new DefaultCatClientFactory.CatClientFactoryHandler(factory);
    }

}
