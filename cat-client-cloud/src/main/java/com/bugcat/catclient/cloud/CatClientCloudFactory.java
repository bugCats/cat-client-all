package com.bugcat.catclient.cloud;

import com.bugcat.catclient.handler.ResultProcessor;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.catclient.spi.CatClientFactory;
import com.bugcat.catclient.spi.CatHttp;
import com.bugcat.catclient.spi.ServerChoose;
import com.bugcat.catclient.utils.CatClientUtil;
import org.springframework.stereotype.Component;

@Component
public class CatClientCloudFactory extends CatClientFactory{

    
    protected static class Inner {
        
        private static ServerChoose chooser = CatClientUtil.getBean(ServerChoose.class);
        private static CloudResultHandler resultHandler = new CloudResultHandler();
        static {
            if( chooser == null ){
                throw new RuntimeException("未找到负载均衡对象 ServiceInstanceChooser");
            }
        }
    }
    
    
    
    @Override
    protected CatHttp catHttp() {
        return super.catHttp();
    }

    
    @Override
    protected SendProcessor sendHandler() {
        return new CloudSendHandler(Inner.chooser);
    }

    
    @Override
    protected ResultProcessor resultHandler() {
        return Inner.resultHandler;
    }

    
}
