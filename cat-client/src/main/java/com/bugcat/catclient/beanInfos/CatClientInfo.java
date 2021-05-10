package com.bugcat.catclient.beanInfos;

import com.bugcat.catclient.annotation.CatClient;
import com.bugcat.catclient.handler.CatMethodInterceptor;
import com.bugcat.catclient.handler.RequestLogs;
import com.bugcat.catclient.spi.CatClientFactory;
import com.bugcat.catclient.spi.DefaultConfiguration;
import com.bugcat.catclient.utils.CatClientUtil;
import com.bugcat.catface.annotation.CatResponesWrapper;
import com.bugcat.catface.spi.AbstractResponesWrapper;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.StandardAnnotationMetadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 注解信息，单例 
 * {@link CatClient}
 * 
 * @author bugcat
 * */
public final class CatClientInfo {
    
    private final String host;
    
    private final int connect;
    private final int socket;
    
    private final RequestLogs logs;
    
    private final String[] tags;
    
    private final Class<? extends CatClientFactory> factoryClass;   //处理类
    
    private final Class<? extends CatMethodInterceptor> interceptor;
    
    private final Class fallback;
    private final boolean fallbackMod;    //是否启用了fallback模式


    /**
     * 响应包装器类处理
     * {@link AbstractResponesWrapper}
     * */
    private final Class<? extends AbstractResponesWrapper> wrapper;



    private CatClientInfo(AnnotationAttributes attr, Properties prop){
        
        //全局默认配置
        DefaultConfiguration config = CatClientUtil.getBean(DefaultConfiguration.class);
        
        String host = attr.getString("host");
        this.host = prop.getProperty(host);
        
        int connect = attr.getNumber("connect");
        connect = connect < 0 ? -1 : connect;
        this.connect = DefaultConfiguration.connect == connect ? config.connect() : connect;

        int socket = attr.getNumber("socket");
        socket = socket < 0 ? -1 : socket;
        this.socket = DefaultConfiguration.socket == socket ? config.socket() : socket;
        
        RequestLogs logs = attr.getEnum("logs");
        logs = DefaultConfiguration.logs.equals(logs) ? config.logs() : logs;
        this.logs = RequestLogs.Def.equals(logs) ? RequestLogs.All2 : logs;
        
        this.tags = attr.getStringArray("tags");
        
        Class<? extends CatClientFactory> factoryClass = attr.getClass("factory");
        this.factoryClass = DefaultConfiguration.factory.equals(factoryClass) ? config.clientFactory() : factoryClass;

        Class<? extends CatMethodInterceptor> interceptor = attr.getClass("interceptor");
        this.interceptor = DefaultConfiguration.interceptor.equals(interceptor) ? config.interceptor() : interceptor;

        this.fallback = attr.getClass("fallback");
        this.fallbackMod = fallback != Object.class;
        
        //响应包装器类，如果是ResponesWrapper.default，代表没有设置
        Class<? extends AbstractResponesWrapper> wrapper = attr.getClass("wrapper");
        wrapper = DefaultConfiguration.wrapper.equals(wrapper) ? config.wrapper() : wrapper;
        this.wrapper = wrapper == DefaultConfiguration.wrapper ? null : wrapper;
        
    }

    
    
    /**
     * 构建CatClientInfo对象
     * */
    public final static CatClientInfo build(Class inter, Properties prop) {
        return build(inter, null, prop);
    }
    
    public final static CatClientInfo build(Class inter, CatClient client, Properties prop) {
        if( client == null ){
            client = (CatClient) inter.getAnnotation(CatClient.class);
        }
        Map<String, Object> attrMap = AnnotationUtils.getAnnotationAttributes(client);
        AnnotationAttributes attributes = getAttributes(inter, attrMap);
        CatClientInfo clientInfo = new CatClientInfo(attributes, prop);
        return clientInfo;
    }
    

    private static AnnotationAttributes getAttributes(Class inter, Map<String, Object> attrMap) {
        /**
         * 如果通过{@link CatClients}生成，则attrMap为null
         * */
        if( attrMap == null ){ 
            attrMap = new HashMap<>();
        }
        AnnotationAttributes client = new AnnotationAttributes(attrMap);
        Map<String, Object> wrapper = responesWrap(inter);
        if( wrapper != null ){
            client.put("wrapper", wrapper.get("value"));
        } else {
            client.put("wrapper", DefaultConfiguration.wrapper);
        }
        return client;
    }

    /**
     * 递归遍历父类、以及interface，获取@CatResponesWrapper注解
     * */
    private static Map<String, Object> responesWrap(Class inter){
        StandardAnnotationMetadata metadata = new StandardAnnotationMetadata(inter);
        Map<String, Object> wrapper = metadata.getAnnotationAttributes(CatResponesWrapper.class.getName());
        if( wrapper == null ){
            for ( Class clazz : inter.getInterfaces() ) {
                wrapper = responesWrap(clazz);
                if( wrapper != null ){
                    return wrapper;
                }
            }
        }
        return wrapper;
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
    public Class getFallback() {
        return fallback;
    }
    public boolean isFallbackMod() {
        return fallbackMod;
    }
    public Class<? extends AbstractResponesWrapper> getWrapper() {
        return wrapper;
    }

}
