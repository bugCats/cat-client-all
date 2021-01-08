package com.bugcat.catclient.cloud;

import com.bugcat.catclient.beanInfos.CatMethodInfo;
import com.bugcat.catclient.beanInfos.CatParameter;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.catclient.spi.ServerChoose;


/**
 * 多例
 * */
public class CloudSendHandler extends SendProcessor {
    
    private ServerChoose chooser;
    
    private String serviceName;
    private String hostAddr;
    private String path;

    
    public CloudSendHandler(ServerChoose chooser) {
        this.chooser = chooser;
    }
    
    @Override
    public void setConfigInfo(CatMethodInfo methodInfo, CatParameter param) {
        super.setConfigInfo(methodInfo, param);

        serviceName = methodInfo.getHost();
        path = param.getPath();
        hostAddr = chooser.hostAddr(serviceName);
        
        super.path = hostAddr + path;
    }

    
    public void chooseOtherHost(){
        hostAddr = chooser.hostAddr(serviceName, hostAddr);
        super.path = hostAddr + path;
    }
    
}
