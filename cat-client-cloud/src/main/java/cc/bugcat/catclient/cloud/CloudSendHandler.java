package cc.bugcat.catclient.cloud;

import cc.bugcat.catclient.beanInfos.CatMethodInfo;
import cc.bugcat.catclient.beanInfos.CatParameter;
import cc.bugcat.catclient.handler.CatSendContextHolder;
import cc.bugcat.catclient.handler.CatSendProcessor;
import cc.bugcat.catclient.spi.CatHttp;
import cc.bugcat.catclient.spi.CatHttpPoint;
import cc.bugcat.catclient.spi.ServerChoose;


/**
 * 多例
 * */
public class CloudSendHandler extends CatSendProcessor {

    private ServerChoose chooser;

    private CatInstanceEntry instanceEntry;
    private String path;


    public CloudSendHandler() {
        this.chooser = getServerChoose();
    }

    public CloudSendHandler(ServerChoose chooser) {
        this.chooser = chooser;
    }

    @Override
    protected void afterVariableResolver(CatSendContextHolder context, CatHttpPoint httpPoint) {

        CatMethodInfo methodInfo = context.getMethodInfo();

        this.path = httpPoint.getPath();

        this.instanceEntry = new CatInstanceEntry(methodInfo.getHost());
        String ipAddr = chooser.hostAddr(instanceEntry);
        this.instanceEntry.ipAddrResolver(ipAddr);
        super.getHttpPoint().setPath(instanceEntry.getHostAddr() + path);
    }


    public void chooseOtherHost(){
        String ipAddr = chooser.retryHostAddr(instanceEntry);
        this.instanceEntry.ipAddrResolver(ipAddr);
        super.getHttpPoint().setPath(instanceEntry.getHostAddr() + path);
    }


    protected ServerChoose getServerChoose(){
        return CatClientCloudFactory.getServerChoose();
    }

}
