package cc.bugcat.catclient.cloud;

import cc.bugcat.catclient.spi.CatResultProcessor;
import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.catclient.spi.DefaultCatClientFactory;
import cc.bugcat.catclient.spi.ServerChoose;
import cc.bugcat.catclient.utils.CatClientUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;



/**
 * 
 * 负载均衡版。
 * 
 * 默认情况下，整合Ribbon，只需要在RestTemplate添加{@code @LoadBalanced}即可。
 * 
 * 如果不是Ribbon、或者不是RestTemplate，就需要手动添加负载均衡器了 {@link ServerChoose}
 * 
 * 
 * @author bugcat
 * */
@Component
public class CatClientCloudFactory extends DefaultCatClientFactory implements InitializingBean {

    private CatResultProcessor resultHandler;
    private ServerChoose chooser;


    @Override
    public void afterPropertiesSet() throws Exception {
        this.resultHandler = new CloudResultHandler();
        this.chooser = CatClientUtil.getBean(ServerChoose.class);
        if( chooser == null ){
            throw new RuntimeException("未找到负载均衡对象 ServerChoose，或者有存在多个！");
        }
    }



    @Override
    public Supplier<CatSendProcessor> newSendHandler() {
        return () -> new CloudSendHandler(chooser);
    }


    @Override
    public CatResultProcessor getResultHandler() {
        return resultHandler;
    }

}
