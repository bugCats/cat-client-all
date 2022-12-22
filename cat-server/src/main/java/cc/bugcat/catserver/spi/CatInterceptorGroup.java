package cc.bugcat.catserver.spi;

import cc.bugcat.catserver.annotation.CatServer;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.handler.CatInterceptPoint;

import java.util.List;
import java.util.function.Supplier;


/**
 * 在运行时匹配的拦截器组。
 *
 * 默认在所有拦截器前执行，可以通过{@code CatServerInterceptor.Group.class}手动指定顺序；
 * 
 * 通过{@link CatServerConfiguration}配置
 * 
 * @see CatServer
 * 
 * @author bugcat
 * */
public interface CatInterceptorGroup {


    /**
     * 匹配分组
     * */
    default boolean matcher(CatInterceptPoint interceptPoint){
        return false;
    }

    /**
     * 如果匹配上，则执行这些拦截器
     * */
    default Supplier<List<CatServerInterceptor>> getInterceptorFactory(){
        return () -> null;
    }



    /**
     * 排序，越小越先执行
     * */
    default int getOrder(){
        return 0;
    }
    
}
