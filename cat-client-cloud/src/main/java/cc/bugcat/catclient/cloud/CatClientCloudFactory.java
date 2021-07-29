package cc.bugcat.catclient.cloud;

import cc.bugcat.catclient.handler.AbstractResultProcessor;
import cc.bugcat.catclient.handler.SendProcessor;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatHttp;
import cc.bugcat.catclient.spi.ServerChoose;
import cc.bugcat.catclient.utils.CatClientUtil;
import org.springframework.stereotype.Component;

@Component
public class CatClientCloudFactory extends CatClientFactory{
    
    protected static class Inner {
        private static final ServerChoose chooser;
        private static final CloudResultHandler resultHandler;
        static {
            chooser = CatClientUtil.getBean(ServerChoose.class);
            if( chooser == null ){
                throw new RuntimeException("未找到负载均衡对象 ServerChoose，或者有存在多个！");
            }
            resultHandler = new CloudResultHandler();
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
    protected AbstractResultProcessor resultHandler() {
        return Inner.resultHandler;
    }

    
    public final static ServerChoose getServerChoose(){
        return Inner.chooser;
    }

}
