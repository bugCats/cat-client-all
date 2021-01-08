package com.bugcat.catclient.cloud;

import com.bugcat.catclient.config.CatHttpRetryConfigurer;
import com.bugcat.catclient.handler.CatHttpException;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.catclient.spi.DefaultResultHandler;

public class CloudResultHandler extends DefaultResultHandler {


    @Override
    public boolean canRetry(CatHttpRetryConfigurer retryConfigurer, CatHttpException ex, SendProcessor sendHandler) throws CatHttpException {
        boolean retry = super.canRetry(retryConfigurer, ex, sendHandler);
        ((CloudSendHandler) sendHandler).chooseOtherHost();
        return retry;
    }
    
}
