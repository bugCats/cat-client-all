package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatHttp;
import cc.bugcat.catclient.spi.CatJsonResolver;
import cc.bugcat.catclient.spi.CatLoggerProcessor;


/**
 *
 * @author bugcat
 * */
public class CatClientFactorys {


    public static CatClientFactory defaultClientFactory(){
        return new DefaultCatClientFactory();
    }



    public static CatClientFactory decorator(CatClientFactory factory){
        return new CatClientFactoryDecorator(factory);
    }


    /**
     * 装饰原始的CatClientFactory对象
     * */
    public final static class CatClientFactoryDecorator implements CatClientFactory {

        private final CatClientFactory bridge;

        private final CatLoggerProcessor loggerProcessor;
        private final CatResultProcessor resultHandler;
        private final CatJsonResolver jsonResolver;
        private final CatHttp catHttp;

        private CatClientFactoryDecorator(CatClientFactory bridge) {
            this.bridge = bridge;
            this.catHttp = bridge.getCatHttp();
            this.loggerProcessor = bridge.getLoggerProcessor();
            this.resultHandler = bridge.getResultHandler();
            this.jsonResolver = bridge.getJsonResolver();
        }


        /**
         * http 类
         * */
        @Override
        public CatHttp getCatHttp(){
            return this.catHttp;
        }

        /**
         * 如果在定义请求方法时，没有传入请求发送类，则在代理类中，自动生成一个请求发送类对象
         * */
        @Override
        public CatSendProcessor newSendHandler(){
            return bridge.newSendHandler();
        }

        /**
         * 日志处理器
         * */
        @Override
        public CatLoggerProcessor getLoggerProcessor() {
            return this.loggerProcessor;
        }

        /**
         * 获取结果处理类
         * */
        @Override
        public CatResultProcessor getResultHandler(){
            return this.resultHandler;
        }

        /**
         * 获取对象序列化处理类
         * */
        @Override
        public CatJsonResolver getJsonResolver(){
            return this.jsonResolver;
        }


        @Override
        public void setClientConfiguration(CatClientConfiguration clientConfiguration) {

        }
    }



}
