package cc.bugcat.catserver.spi;

import cc.bugcat.catface.spi.AbstractResponesWrapper;

/**
 * CatServer类返回对象处理
 *
 * 无包装器类
 *
 *
 * @author bugcat
 * */
public interface CatResultHandler {

            
    default void setResponesWrapper(AbstractResponesWrapper wrapperHandler){
        
    }
    

    default Object onSuccess(Object value, Class returnType){
        return value;
    }

    default Object onError(Throwable throwable, Class returnType) throws Throwable{
        throw throwable;
    }


    /**
     * 无包装器默认情况
     * */
    public static final class Default implements CatResultHandler {
        
    }
    
}
