package cc.bugcat.catserver.spi;


import cc.bugcat.catface.spi.AbstractResponesWrapper;

/**
 * 无包装器默认情况
 * */
public class DefaultResultHandler implements CatResultHandler {

    public void setResponesWrapper(AbstractResponesWrapper wrapperHandler){

    }


    public Object onSuccess(Object value, Class methodReturnClass){
        return value;
    }

    public Object onError(Throwable throwable, Class methodReturnClass) throws Throwable{
        throw throwable;
    }
    
}
