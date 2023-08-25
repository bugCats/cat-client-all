package cc.bugcat.catclient.spi;

import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.handler.CatClientLogger;
import cc.bugcat.catclient.handler.CatLogsMod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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


    default void printLog(CatClientLogger catLog) {
        if ( CatLogsMod.Off.equals(catLog.getLogsMod()) ) {
            // 关闭所有http日志
        } else {
//            Logger logger = LoggerFactory.getLogger(logger.getClientClass()); // 会存在大量logger对象导致内存溢出？
            Logger logger = catLog.getLogger();
            if( catLog.isFail() ){
                logger.error(catLog.toJson());
            } else {
                logger.info(catLog.toJson());
            }
        }
    }

}
