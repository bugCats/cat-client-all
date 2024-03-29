package cc.bugcat.catclient.cloud;

import cc.bugcat.catclient.handler.CatClientContextHolder;
import cc.bugcat.catclient.exception.CatHttpException;
import cc.bugcat.catclient.spi.SimpleResultHandler;


/**
 * cloud模式中，结果处理类
 *
 * @author bugcat
 * */
public class CloudResultHandler extends SimpleResultHandler {


    /**
     * 判断是否需要重连
     *
     * 如果需要重连，尝试调用{@link CloudSendHandler#chooseOtherHost()}方法，重新选择一个实例，再次执行http请求
     * */
    @Override
    public boolean canRetry(CatHttpException exception, CatClientContextHolder context) {
        boolean retry = super.canRetry(exception, context);
        if ( retry ) {
            ((CloudSendHandler) context.getSendHandler()).chooseOtherHost();
        }
        return retry;
    }

}
