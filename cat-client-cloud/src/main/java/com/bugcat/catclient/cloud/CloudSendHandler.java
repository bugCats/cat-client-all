package com.bugcat.catclient.cloud;

import com.bugcat.catclient.beanInfos.CatMethodInfo;
import com.bugcat.catclient.beanInfos.CatParameter;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.catclient.spi.ServerChoose;


/**
 * 多例
 * */
public class CloudSendHandler extends SendProcessor{
    
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
    public void setConfigInfo(CatMethodInfo methodInfo, CatParameter param) {
        super.setConfigInfo(methodInfo, param);

        instanceEntry = new CatInstanceEntry(methodInfo.getHost());
        path = param.getPath();
        
        String ipAddr = chooser.hostAddr(instanceEntry);
        instanceEntry.setIpAddr(ipAddr);
        super.path = instanceEntry.getHostAddr() + path;
    }

    
    public void chooseOtherHost(){
        String ipAddr = chooser.retryHostAddr(instanceEntry);
        instanceEntry.setIpAddr(ipAddr);
        super.path = instanceEntry.getHostAddr() + path;
    }


    protected ServerChoose getServerChoose(){
        return CatClientCloudFactory.getServerChoose();
    }
    
}
