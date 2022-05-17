package cc.bugcat.catclient.cloud;

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

    private ServerChoose chooser;

    private CatInstanceResolver instanceEntry;

    public CloudSendHandler(ServerChoose chooser) {
        this.chooser = chooser;
    }


    /**
     * 将服务名修改成实际的ip+端口
     * */
    @Override
    public void postVariableResolver(CatSendContextHolder context){
        CatHttpPoint httpPoint = super.getHttpPoint();
        instanceEntry = new CatInstanceResolver(httpPoint.getHost());
        String ipAddr = chooser.hostAddr(instanceEntry);
        String sendHost = instanceEntry.resolver(ipAddr);
        httpPoint.setHost(sendHost);
    }


    /**
     * 重新选择另外一个实例
     * */
    public void chooseOtherHost(){
        String ipAddr = chooser.retryHostAddr(instanceEntry);
        String sendHost = instanceEntry.resolver(ipAddr);
        super.getHttpPoint().setHost(sendHost);
    }


}
