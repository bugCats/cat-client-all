package cc.bugcat.catclient.cloud;

import cc.bugcat.catclient.beanInfos.CatParameter;
import cc.bugcat.catclient.handler.CatSendContextHolder;
import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.catclient.handler.CatHttpPoint;
import cc.bugcat.catclient.spi.ServerChoose;


/**
 * cloud模式发送类
 *
 * @author bugcat
 * */
public class CloudSendHandler extends CatSendProcessor {

    private ServerChoose serverChoose;

    private CatInstanceResolver instanceEntry;

    public CloudSendHandler(ServerChoose serverChoose) {
        this.serverChoose = serverChoose;
    }

    
    @Override
    public void postConfigurationResolver(CatSendContextHolder context, CatParameter parameter) {
        super.postConfigurationResolver(context, parameter);

        /**
         * 将服务名修改成实际的ip+端口
         * */
        CatHttpPoint httpPoint = super.getHttpPoint();
        this.instanceEntry = new CatInstanceResolver(httpPoint.getHost());
        String ipAddr = serverChoose.hostAddr(instanceEntry);
        String sendHost = instanceEntry.resolver(ipAddr);
        httpPoint.setHost(sendHost);
    }


    /**
     * 重新选择另外一个实例
     * */
    public void chooseOtherHost(){
        String ipAddr = serverChoose.retryHostAddr(instanceEntry);
        String sendHost = instanceEntry.resolver(ipAddr);
        super.getHttpPoint().setHost(sendHost);
    }


}
