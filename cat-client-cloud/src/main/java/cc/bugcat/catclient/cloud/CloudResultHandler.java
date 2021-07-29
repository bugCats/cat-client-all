package cc.bugcat.catclient.cloud;

import cc.bugcat.catclient.handler.SendProcessor;
import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.config.CatHttpRetryConfigurer;
import cc.bugcat.catclient.handler.CatHttpException;
import cc.bugcat.catclient.spi.DefaultResultHandler;

public class CloudResultHandler extends DefaultResultHandler{


    @Override
    public boolean canRetry(CatHttpRetryConfigurer retryConfigurer, CatHttpException ex, CatClientInfo clientInfo, SendProcessor sendHandler) {
        boolean retry = super.canRetry(retryConfigurer, ex, clientInfo, sendHandler);
        ((CloudSendHandler) sendHandler).chooseOtherHost();
        return retry;
    }
    
}
