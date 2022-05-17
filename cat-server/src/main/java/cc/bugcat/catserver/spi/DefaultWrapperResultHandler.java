package cc.bugcat.catserver.spi;

import cc.bugcat.catface.spi.AbstractResponesWrapper;


/**
 * 存在包装器类默认情况
 * */
public class DefaultWrapperResultHandler implements CatResultHandler {

    private AbstractResponesWrapper wrapperHandler;

    @Override
    public void setResponesWrapper(AbstractResponesWrapper wrapperHandler) {
        this.wrapperHandler = wrapperHandler;
    }

    @Override
    public Object onSuccess(Object value, Class returnType){
        if( value != null ){
            Class<?> returnClass = value.getClass();
            Class wrapperClass = wrapperHandler.getWrapperClass();
            if( wrapperClass.equals(returnClass) || wrapperClass.isAssignableFrom(returnClass)){
                return value;
            } else {
                return wrapperHandler.createEntryOnSuccess(value, returnType);
            }
        } else {
            return wrapperHandler.createEntryOnSuccess(value, returnType);
        }
    }

    @Override
    public Object onError(Throwable throwable, Class returnType) throws Throwable {
        return wrapperHandler.createEntryOnException(throwable, returnType);
    }

}
