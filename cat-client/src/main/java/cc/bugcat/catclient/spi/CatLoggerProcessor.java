package cc.bugcat.catclient.spi;

import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.handler.CatClientLogger;
import cc.bugcat.catclient.handler.CatLogsMod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 打印http日志
 * 
 * 日志默认格式{@link CatClientLogger#toJson()}。
 * 
 * 可以在此子类中修改，再在{@link CatClientConfiguration#getLoggerProcessor()}指定
 *
 * @author bugcat
 * */
public interface CatLoggerProcessor {

    static final Log LOGGER = LogFactory.getLog(CatLoggerProcessor.class);


    default void printLog(CatClientLogger logger) {
        if ( CatLogsMod.Off.equals(logger.getLogsMod()) ) {
            // 关闭所有http日志
        } else {
            if( logger.isFail() ){
                LOGGER.error(logger.toJson());
            } else {
                LOGGER.info(logger.toJson());
            }
        }
    }

}
