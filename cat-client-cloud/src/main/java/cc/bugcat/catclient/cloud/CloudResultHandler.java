package cc.bugcat.catclient.cloud;

import cc.bugcat.catclient.handler.CatSendContextHolder;
import cc.bugcat.catclient.config.CatHttpRetryConfigurer;
import cc.bugcat.catclient.handler.CatHttpException;
import cc.bugcat.catclient.spi.DefaultResultHandler;

public class CloudResultHandler extends DefaultResultHandler{


    @Override
    public boolean canRetry(CatHttpException exception, CatSendContextHolder context) {
        boolean retry = super.canRetry(exception, context);
        ((CloudSendHandler) context.getSendHandler()).chooseOtherHost();
        return retry;
    }

}
