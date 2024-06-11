package cc.bugcat.catclient.beanInfos;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.handler.CatLogsMod;
import cc.bugcat.catclient.handler.CatMethodAopInterceptor;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatResultProcessor;
import cc.bugcat.catclient.spi.CatSendInterceptor;
import cc.bugcat.catclient.utils.CatClientBuilders;
import cc.bugcat.catface.annotation.CatNote;
import cc.bugcat.catface.utils.CatToosUtil;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

/**
 * 动态创建{@code @CatClient}实例。
 * 配合{@link CatClientBuilders}使用。
 * */
public class CatClients {

    private String value;

    private String host;
    
    private Class<? extends CatClientFactory> factory;

    private Class<? extends CatSendInterceptor> interceptor;

    private Class fallback;

    private Integer socket;

    private Integer connect;

    private CatLogsMod logsMod;

    private CatNote[] tags;

    
    
    private CatClients(){
        // noop
    }
    
    
    
    public static CatClients builder(){
        return new CatClients();
    }

    
    
    public CatClients value(String value) {
        this.value = value;
        return this;
    }
    public CatClients host(String host) {
        this.host = host;
        return this;
    }
    public CatClients factory(Class<? extends CatClientFactory> factory) {
        this.factory = factory;
        return this;
    }
    public CatClients interceptor(Class<? extends CatSendInterceptor> interceptor) {
        this.interceptor = interceptor;
        return this;
    }
    public CatClients fallback(Class fallback) {
        this.fallback = fallback;
        return this;
    }
    public CatClients socket(int socket) {
        this.socket = socket;
        return this;
    }
    public CatClients connect(int connect) {
        this.connect = connect;
        return this;
    }
    public CatClients logsMod(CatLogsMod logsMod) {
        this.logsMod = logsMod;
        return this;
    }
    public CatClients tags(CatNote[] tags) {
        this.tags = tags;
        return this;
    }
    
    
    public CatClient build(){
        if( CatToosUtil.isBlank(host) ){
            throw new IllegalArgumentException(CatClients.class.getName() + "#host can not empty!");
        }
        if( value == null ){
            value = "";
        }
        if( factory == null ){
            factory = CatClientFactory.class;
        }
        if( interceptor == null ){
            interceptor = CatSendInterceptor.class;
        }
        if( fallback == null ){
            fallback = Object.class;
        }
        if( socket == null ){
            socket = CatClientConfiguration.SOCKET;
        }
        if( connect == null ){
            connect = CatClientConfiguration.CONNECT;
        }
        if( logsMod == null ){
            logsMod = CatLogsMod.Def;
        }
        if( tags == null ){
            tags = new CatNote[0];
        }
        return new CatClient(){
            @Override
            public String value() {
                return value;
            }
            @Override
            public String host() {
                return host;
            }
            @Override
            public Class<? extends CatClientFactory> factory() {
                return factory;
            }
            @Override
            public Class<? extends CatSendInterceptor> interceptor() {
                return interceptor;
            }
            @Override
            public Class fallback() {
                return fallback;
            }
            @Override
            public int socket() {
                return socket;
            }
            @Override
            public int connect() {
                return connect;
            }
            @Override
            public CatLogsMod logsMod() {
                return logsMod;
            }
            @Override
            public CatNote[] tags() {
                return tags;
            }
            @Override
            public Class<? extends Annotation> annotationType() {
                return CatClient.class;
            }
        };
    }
    
}
