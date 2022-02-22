package cc.bugcat.catclient.spi;

import cc.bugcat.catclient.handler.CatClientLogger;
import cc.bugcat.catclient.handler.CatLogsMod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 打印http日志
 *
 * @author bugcat
 * */
public interface CatLoggerProcessor {

    static final Log LOGGER = LogFactory.getLog(CatLoggerProcessor.class);


    default void printLog(CatClientLogger logger) {
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
