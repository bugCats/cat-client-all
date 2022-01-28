package cc.bugcat.catclient.handler;


import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.spi.*;


/**
 * 工厂，设置请求发送类、结果响应类
 *
 * @author bugcat
 * */
public class DefaultCatClientFactory implements CatClientFactory {

    private CatClientConfiguration configuration;

    @Override
    public void setClientConfiguration(CatClientConfiguration clientConfiguration) {
        this.configuration = clientConfiguration;
    }

    @Override
    public CatHttp getCatHttp() {
        return configuration.catHttp();
    }

    @Override
    public CatJsonResolver getJsonResolver() {
        return configuration.jsonResolver();
    }

    @Override
    public CatSendProcessor newSendHandler() {
        return new DefaultSendHandler();
    }

    @Override
    public CatLoggerProcessor getLoggerProcessor() {
        return configuration.loggerProcessor();
    }

    @Override
    public AbstractCatResultProcessor getResultHandler() {
        return new DefaultResultHandler();
    }





    public final static class CatClientFactoryHandler implements CatClientFactory {

        private final CatClientFactory bridge;

        private final CatLoggerProcessor loggerProcessor;
        private final AbstractCatResultProcessor resultHandler;
        private final CatJsonResolver jsonResolver;
        private final CatHttp catHttp;

        public CatClientFactoryHandler(CatClientFactory bridge) {
            this.bridge = bridge;
            this.catHttp = bridge.getCatHttp();
            this.loggerProcessor = bridge.getLoggerProcessor();
            this.resultHandler = bridge.getResultHandler();
            this.jsonResolver = bridge.getJsonResolver();
        }

        /**
         * http 类
         * 单例
         * */
        @Override
        public CatHttp getCatHttp(){
            return this.catHttp;
        }


        /**
         * 如果在定义请求方法时，没有传入请求发送类，则在代理类中，自动生成一个请求发送类对象
         * 多例
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
         * 单例
         * */
        @Override
        public AbstractCatResultProcessor getResultHandler(){
            return this.resultHandler;
        }

        /**
         * 获取对象序列化处理类
         * 单例
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
