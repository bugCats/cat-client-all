package cc.bugcat.catclient.cloud;

import cc.bugcat.catclient.spi.CatResultProcessor;
import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.catclient.spi.SimpleClientFactory;
import cc.bugcat.catclient.spi.ServerChoose;
import cc.bugcat.catclient.utils.CatClientUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @author bugcat
 * */
@Component
public class ClientCloudFactory extends SimpleClientFactory implements InitializingBean {

    @Autowired(required = false)
    private ServerChoose serverChoose;

    @Override
    public void afterPropertiesSet() {
        if( serverChoose == null ){
            serverChoose = CatClientUtil.getBean(ServerChoose.class);
            if( serverChoose == null ){
                throw new RuntimeException("未找到负载均衡对象 ServerChoose，或者有存在多个！");
            }
        }
    }
    
    /**
     * cloud 获取发送类
     * */
    @Override
    public Supplier<CatSendProcessor> newSendHandler() {
        return newSendHandler(serverChoose);
    }
    
    
    protected Supplier<CatSendProcessor> newSendHandler(ServerChoose serverChoose) {
        return () -> new CloudSendHandler(serverChoose);
    }

    
    @Override
    public CatResultProcessor getResultHandler() {
        return new CloudResultHandler();
    }
    
    public ServerChoose getServerChoose() {
        return serverChoose;
    }
    
}
