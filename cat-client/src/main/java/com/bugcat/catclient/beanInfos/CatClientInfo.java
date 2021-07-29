package com.bugcat.catclient.beanInfos;

import com.bugcat.catclient.annotation.CatClient;
import com.bugcat.catclient.handler.CatMethodInterceptor;
import com.bugcat.catclient.handler.RequestLogs;
import com.bugcat.catclient.spi.CatClientFactory;
import com.bugcat.catclient.spi.DefaultConfiguration;
import com.bugcat.catclient.utils.CatClientUtil;
import com.bugcat.catface.annotation.CatResponesWrapper;
import com.bugcat.catface.annotation.Catface;
import com.bugcat.catface.spi.AbstractResponesWrapper;
import com.bugcat.catface.utils.CatToosUtil;

import java.util.Map;
import java.util.Properties;

/**
 * 注解信息，单例 
 * {@link CatClient}
 * 
 * @author bugcat
 * */
public final class CatClientInfo {
    
    private final String serviceName;
    
    private final String host;  //远程服务器主机
    
    private final int connect;  //连接超时
    private final int socket;   //读取超时
    
    private final RequestLogs logs; //日志记录方案
    
    private final String[] tags;    //api标签归类
    
    private final Class<? extends CatClientFactory> factoryClass;   //http发送工厂类
    
    private final Class<? extends CatMethodInterceptor> interceptor;    //http发送流程类

    private final boolean fallbackMod;    //是否启用了fallback模式
    private final Class fallback;       //回调类

    /**
     * 响应包装器类处理
     * {@link AbstractResponesWrapper}
     * */
    private final Class<? extends AbstractResponesWrapper> wrapper;

    /**
     * 是否使用精简模式
     * */
    private final Catface catface;
    

    
    
    private CatClientInfo(CatClient client, Map<String, Object> paramMap, Properties prop){
        
        //全局默认配置
        DefaultConfiguration config = CatClientUtil.getBean(DefaultConfiguration.class);
        
        this.serviceName = (String) paramMap.get("serviceName");
        
        String host = client.host();
        this.host = prop.getProperty(host);
        
        int connect = client.connect();
        connect = connect < 0 ? -1 : connect;
        this.connect = DefaultConfiguration.connect == connect ? config.connect() : connect;

        int socket = client.socket();
        socket = socket < 0 ? -1 : socket;
        this.socket = DefaultConfiguration.socket == socket ? config.socket() : socket;
        
        RequestLogs logs = client.logs();
        logs = DefaultConfiguration.logs.equals(logs) ? config.logs() : logs;
        this.logs = RequestLogs.Def.equals(logs) ? RequestLogs.All2 : logs;
        
        this.tags = client.tags();
        
        Class<? extends CatClientFactory> factoryClass = client.factory();
        this.factoryClass = DefaultConfiguration.factory.equals(factoryClass) ? config.clientFactory() : factoryClass;

        Class<? extends CatMethodInterceptor> interceptor = client.interceptor();
        this.interceptor = DefaultConfiguration.interceptor.equals(interceptor) ? config.interceptor() : interceptor;

        this.fallback = client.fallback();
        this.fallbackMod = fallback != Object.class;
        
        //响应包装器类，如果是ResponesWrapper.default，代表没有设置
        CatResponesWrapper responesWrapper = (CatResponesWrapper) paramMap.get("wrapper");
        Class<? extends AbstractResponesWrapper> wrapper = responesWrapper == null ? config.wrapper() : responesWrapper.value();
        this.wrapper = wrapper == DefaultConfiguration.wrapper ? null : wrapper;
        
        this.catface = (Catface) paramMap.get("catface");
    }

    
    
    /**
     * 构建CatClientInfo对象
     * */
    public final static CatClientInfo build(Class inter, Properties prop) {
        return build(inter, null, prop);
    }
    
    
    /**
     * 构建CatClientInfo对象
     * @param inter     interface
     * @param client    可以为null。为null表示直接使用interface上的注解；否则，该interface，强制使用传入的client注解
     * @param prop      环境变量
     * */
    public final static CatClientInfo build(Class inter, CatClient client, Properties prop) {
        if( client == null ){ 
            client = (CatClient) inter.getAnnotation(CatClient.class);
        }
        Map<String, Object> paramMap = CatToosUtil.getAttributes(inter);
        paramMap.put("serviceName", inter.getSimpleName());
        CatClientInfo clientInfo = new CatClientInfo(client, paramMap, prop);
        return clientInfo;
    }


    public String getServiceName() {
        return serviceName;
    }
    public String getHost () {
        return host;
    }
    public int getConnect () {
        return connect;
    }
    public int getSocket () {
        return socket;
    }
    public RequestLogs getLogs() {
        return logs;
    }
    public String[] getTags() {
        return tags;
    }
    public Class<? extends CatClientFactory> getFactoryClass() {
        return factoryClass;
    }
    public Class<? extends CatMethodInterceptor> getInterceptor() {
        return interceptor;
    }
    public boolean isFallbackMod() {
        return fallbackMod;
    }
    public Class getFallback() {
        return fallback;
    }
    public Class<? extends AbstractResponesWrapper> getWrapper() {
        return wrapper;
    }
    public Catface getCatface() {
        return catface;
    }
}
