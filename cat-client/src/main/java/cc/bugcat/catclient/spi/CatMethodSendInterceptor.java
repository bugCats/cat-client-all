package cc.bugcat.catclient.spi;


import cc.bugcat.catclient.handler.CatHttpException;
import cc.bugcat.catclient.handler.CatSendContextHolder;


/**
 * 发送http请求时拦截器
 *
 *
 * @author bugcat
 * */
public interface CatMethodSendInterceptor {



    /**
     * 拦截器中处理参数
     * 每次调用interface的方法，仅执行一次
     * */
    default void executeVariable(CatSendContextHolder context, CatSendProcessor sendProcessor) {
        sendProcessor.postVariableResolver(context);
        sendProcessor.afterVariableResolver(context);
    }


    /**
     * 执行发送http请求
     * 如果启用重连，会执行多次
     * */
    default String executeHttpSend(CatSendProcessor sendProcessor) throws CatHttpException {
        return sendProcessor.postHttpSend();
    }


}
