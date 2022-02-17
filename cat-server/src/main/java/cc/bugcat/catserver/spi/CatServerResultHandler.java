package cc.bugcat.catserver.spi;

import cc.bugcat.catface.spi.AbstractResponesWrapper;


/**
 * CatServer类返回对象处理
 *
 * 无包装器类
 * */
public class CatServerResultHandler {


    public static CatServerResultHandler build(){
        return new CatServerResultHandler();
    }


    public static CatServerResultHandler build(AbstractResponesWrapper wrapperHandler, Class returnType) {
        return new ResultWrapperHandler(wrapperHandler, returnType);
    }



    public Object onSuccess(Object value){
        return value;
    }

    public Object onError(Throwable throwable) throws Throwable{
        throw throwable;
    }



    /**
     * 有包装器类
     * */
    protected static class ResultWrapperHandler extends CatServerResultHandler {

        private final AbstractResponesWrapper wrapperHandler;
        private final Class returnType;

        public ResultWrapperHandler(AbstractResponesWrapper wrapperHandler, Class returnType) {
            this.wrapperHandler = wrapperHandler;
            this.returnType = returnType;
        }

        public Object onSuccess(Object value){
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

        public Object onError(Throwable throwable) throws Throwable{
            return wrapperHandler.createEntryOnException(throwable, returnType);
        }
    }

}
