package cc.bugcat.catclient.spi;


import cc.bugcat.catclient.beanInfos.CatParameter;
import cc.bugcat.catclient.handler.CatClientContextHolder;
import cc.bugcat.catclient.handler.CatHttpException;


/**
 * 发送http请求时拦截器
 * 
 * @author bugcat
 * */
public interface CatSendInterceptor {



    /**
     * 1、拦截器中处理http配置项
     * 每次调用interface的方法，仅执行一次
     * */
    default void executeConfigurationResolver(CatClientContextHolder context, CatParameter parameter, Intercepting processor) throws Exception {
        processor.executeInternal();
    }
    

    /**
     * 2、拦截器中处理参数
     * 每次调用interface的方法，仅执行一次
     * */
    default void executeVariableResolver(CatClientContextHolder context, Intercepting processor) throws Exception {
        processor.executeInternal();
    }


    /**
     * 3、执行发送http请求
     * 如果启用重连，会执行多次
     * */
    default String executeHttpSend(CatSendProcessor sendHandler, HttpIntercepting processor) throws CatHttpException {
        return processor.executeInternal();
    }


    
    
    
    static interface Intercepting {
        void executeInternal() throws Exception;
    }
    static interface HttpIntercepting {
        String executeInternal() throws CatHttpException;
    }

}
