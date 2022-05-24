package cc.bugcat.catclient.spi;


import cc.bugcat.catclient.beanInfos.CatParameter;
import cc.bugcat.catclient.handler.CatHttpException;
import cc.bugcat.catclient.handler.CatSendContextHolder;


/**
 * 发送http请求时拦截器
 * 
 * @author bugcat
 * */
public interface CatMethodSendInterceptor {



    /**
     * 1、拦截器中处理http配置项
     * 每次调用interface的方法，仅执行一次
     * */
    default void executeConfigurationResolver(CatSendContextHolder context, CatParameter parameter){
        context.getSendHandler().doConfigurationResolver(context, parameter);
    }
    

    /**
     * 2、拦截器中处理参数
     * 每次调用interface的方法，仅执行一次
     * */
    default void executeVariableResolver(CatSendContextHolder context) {
        CatSendProcessor sendHandler = context.getSendHandler();
        sendHandler.doVariableResolver(context);
        sendHandler.postVariableResolver(context);
    }


    /**
     * 3、执行发送http请求
     * 如果启用重连，会执行多次
     * */
    default String executeHttpSend(CatSendProcessor sendProcessor) throws CatHttpException {
        return sendProcessor.postHttpSend();
    }


}
