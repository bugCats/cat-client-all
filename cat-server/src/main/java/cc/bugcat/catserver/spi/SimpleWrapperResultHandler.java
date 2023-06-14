package cc.bugcat.catserver.spi;

import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catface.utils.CatToosUtil;


/**
 * 存在包装器类默认情况
 * */
public class SimpleWrapperResultHandler implements CatResultHandler {

    private AbstractResponesWrapper wrapperHandler;

    @Override
    public void setResponesWrapper(AbstractResponesWrapper wrapperHandler) {
        this.wrapperHandler = wrapperHandler;
    }

    @Override
    public Object onSuccess(Object value, Class methodReturnClass){
        Class wrapperClass = wrapperHandler.getWrapperClass();
        if( wrapperClass.equals(methodReturnClass) || wrapperClass.isAssignableFrom(methodReturnClass)){
            return value;
        } else {
            return wrapperHandler.createEntryOnSuccess(value, methodReturnClass);
        }
    }

    @Override
    public Object onError(Throwable throwable, Class methodReturnClass) {
        Throwable ex = CatToosUtil.getCause(throwable);
        return wrapperHandler.createEntryOnException(ex, methodReturnClass);
    }

}
