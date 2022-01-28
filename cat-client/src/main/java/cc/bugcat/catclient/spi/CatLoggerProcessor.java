package cc.bugcat.catclient.spi;

import cc.bugcat.catclient.handler.CatClientLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 打印http日志
 * 在logback中控制日志级别，以及日志格式
 * */
public interface CatLoggerProcessor {

    static Logger LOGGER = LoggerFactory.getLogger(CatLoggerProcessor.class);



    void printLog(CatClientLogger logger);



    static class Default implements CatLoggerProcessor {
        @Override
        public void printLog(CatClientLogger logger) {
            if( logger.isFail() ){
                LOGGER.error(logger.toString());
            } else {
                LOGGER.info(logger.toString());
            }
        }
    }


}
