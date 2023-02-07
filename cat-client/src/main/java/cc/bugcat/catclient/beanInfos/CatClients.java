package cc.bugcat.catclient.beanInfos;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catface.annotation.CatNote;
import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.handler.CatLogsMod;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatSendInterceptors;
import cc.bugcat.catclient.utils.CatClientBuilders;

import java.lang.annotation.Annotation;

/**
 * 动态创建{@code @CatClient}实例。
 * 配合{@link CatClientBuilders}使用。
 * */
public abstract class CatClients implements CatClient {

    @Override
    public String value() {
        return "";
    }
    
    @Override
    public abstract String host();

    
    @Override
    public Class<? extends CatClientFactory> factory() {
        return CatClientFactory.class;
    }

    @Override
    public Class<? extends CatSendInterceptors> interceptor() {
        return CatSendInterceptors.class;
    }

    @Override
    public Class fallback() {
        return Object.class;
    }

    @Override
    public int socket() {
        return CatClientConfiguration.SOCKET;
    }

    @Override
    public int connect() {
        return CatClientConfiguration.CONNECT;
    }

    @Override
    public CatLogsMod logsMod() {
        return CatLogsMod.Def;
    }

    @Override
    public CatNote[] tags() {
        return new CatNote[0];
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return CatClient.class;
    };
}
