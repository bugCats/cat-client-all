package cc.bugcat.catserver.spi;

import cc.bugcat.catserver.handler.CatInterceptPoint;

import java.util.ArrayList;
import java.util.List;


/**
 * CatServer上拦截器组
 * */
public interface CatInterceptorGroup {



    default boolean matcher(CatInterceptPoint interceptPoint){
        return false;
    }


    default List<CatInterceptor> getInterceptors(){
        return new ArrayList<>(0);
    }


    default CatServerResultHandler getResultHandler() {
        return null;
    }


}
