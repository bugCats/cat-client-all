package cc.bugcat.catclient.spi;


import cc.bugcat.catclient.handler.CatHttpException;
import cc.bugcat.catclient.handler.CatSendProcessor;


/**
 * 发送http请求时拦截器
 *
 *
 * @author bugcat
 * */
public interface CatMethodInterceptor {



    String executeInternal(CatSendProcessor sendProcessor) throws CatHttpException;



    class Default implements CatMethodInterceptor {
        @Override
        public String executeInternal(CatSendProcessor sendProcessor) throws CatHttpException {
            return sendProcessor.httpSend();
        }
    }


}
