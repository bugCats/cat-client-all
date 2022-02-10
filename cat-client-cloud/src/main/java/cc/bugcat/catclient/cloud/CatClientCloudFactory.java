package cc.bugcat.catclient.cloud;

import cc.bugcat.catclient.handler.CatResultProcessor;
import cc.bugcat.catclient.handler.CatSendProcessor;
import cc.bugcat.catclient.handler.DefaultCatClientFactory;
import cc.bugcat.catclient.spi.ServerChoose;
import cc.bugcat.catclient.utils.CatClientUtil;
import org.springframework.stereotype.Component;

@Component
public class CatClientCloudFactory extends DefaultCatClientFactory {

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
    public CatSendProcessor newSendHandler() {
        return new CloudSendHandler(Inner.chooser);
    }


    @Override
    public CatResultProcessor getResultHandler() {
        return Inner.resultHandler;
    }


    public final static ServerChoose getServerChoose(){
        return Inner.chooser;
    }

}
