package cc.bugcat.catserver.spi;

/**
 * CatServer类返回对象处理
 *
 * 无包装器类
 *
 *
 * @author bugcat
 * */
public interface CatServerResultHandler {



    default Object onSuccess(Object value, Class returnType){
        return value;
    }

    default Object onError(Throwable throwable, Class returnType) throws Throwable{
        throw throwable;
    }



}
