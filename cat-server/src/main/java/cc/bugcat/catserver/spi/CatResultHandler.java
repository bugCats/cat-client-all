package cc.bugcat.catserver.spi;

import cc.bugcat.catface.spi.AbstractResponesWrapper;

/**
 * CatServer类返回对象处理
 *
 * @author bugcat
 * */
public interface CatResultHandler {

    void setResponesWrapper(AbstractResponesWrapper wrapperHandler);
    
    Object onSuccess(Object value, Class returnType);

    Object onError(Throwable throwable, Class returnType) throws Throwable;

}
