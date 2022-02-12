package cc.bugcat.catclient.spi;

import cc.bugcat.catclient.handler.CatClientLogger;
import cc.bugcat.catclient.handler.CatLogsMod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 打印http日志
 * 在logback中控制日志级别，以及日志格式
 *
 * @author bugcat
 * */
public interface CatLoggerProcessor {

    static Logger LOGGER = LoggerFactory.getLogger(CatLoggerProcessor.class);



    void printLog(CatClientLogger logger);



    static class Default implements CatLoggerProcessor {
        @Override
        public void printLog(CatClientLogger logger) {
            if ( CatLogsMod.Off.equals(logger.getLogsMod()) ) {

            } else {
                if( logger.isFail() ){
                    LOGGER.error(logger.toJson());
                } else {
                    LOGGER.info(logger.toJson());
                }
            }
        }
    }


}
