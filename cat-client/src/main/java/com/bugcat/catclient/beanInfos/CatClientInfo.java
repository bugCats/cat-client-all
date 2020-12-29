package com.bugcat.catclient.beanInfos;

import com.bugcat.catclient.annotation.CatClient;
import com.bugcat.catclient.handler.RequestLogs;
import com.bugcat.catclient.spi.CatClientFactory;
import com.bugcat.catclient.spi.CatDefaultConfiguration;
import com.bugcat.catclient.utils.CatClientUtil;
import com.bugcat.catface.annotation.CatResponesWrapper;
import com.bugcat.catface.spi.ResponesWrapper;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.StandardAnnotationMetadata;

import java.util.Map;
import java.util.Properties;

/**
 * @CatClient 注解信息
 * 单例
 * @author bugcat
 * */
public class CatClientInfo {
    
    private String beanName;            //主键别名
    
    private String host;                //远程接口域名，支持${xx.xx}
    
    private int connect;                //http链接超时
    
    private int socket;                 //http链接超时
    
    private RequestLogs logs;           //日志打印方案
    
    private CatClientFactory factory;   //处理类
    
    private Class fallback;                 //http异常处理类
    private boolean fallbackMod = false;    //是否启用了fallback模式
    
    private Class<? extends ResponesWrapper> wrapper;      //响应包裹类
    
    
    public CatClientInfo(AnnotationAttributes attr, Properties prop){

        CatDefaultConfiguration config = CatClientUtil.getBean(CatDefaultConfiguration.class);
        
        this.beanName = attr.getString("value");
        
        String host = attr.getString("host");
        this.host = prop.getProperty(host);
        
        int connect = attr.getNumber("connect");
        this.connect = connect < 0 ? -1 : connect;
        this.connect = CatDefaultConfiguration.connect.equals(this.connect) ? config.connect() : this.connect;

        int socket = attr.getNumber("socket");
        this.socket = socket < 0 ? -1 : socket;
        this.socket = CatDefaultConfiguration.socket.equals(this.socket) ? config.socket() : this.socket;

        
        RequestLogs logs = attr.getEnum("logs");
        logs = CatDefaultConfiguration.logs.equals(logs) ? config.logs() : logs;
        this.logs = RequestLogs.Def.equals(logs) ? RequestLogs.All2 : logs;
        
        
        Class<? extends CatClientFactory> factory = attr.getClass("factory");
        try {
            this.factory = factory.newInstance(); 
        } catch ( Exception e ) {
            this.factory = new CatClientFactory();
            System.err.println("初始化=" + factory.getSimpleName() + "异常！使用默认工厂！");
        }
        this.factory.setDefaultConfiguration(config);
    
        
        this.fallback = attr.getClass("fallback");
        this.fallbackMod = fallback != Object.class;
        
        
        //响应包裹类，如果是ResponesWrapper.default，代表没有设置
        Class<? extends ResponesWrapper> wrapper = attr.getClass("wrapper");
        wrapper = CatDefaultConfiguration.wrapper.equals(wrapper) ? config.wrapper() : wrapper;
        this.wrapper = wrapper == CatDefaultConfiguration.wrapper ? null : wrapper;
        
    }

    
    public static AnnotationAttributes getAttributes(Class inter) {
        StandardAnnotationMetadata metadata = new StandardAnnotationMetadata(inter);
        AnnotationAttributes client = new AnnotationAttributes(metadata.getAnnotationAttributes(CatClient.class.getName()));
        Map<String, Object> wrapper = responesWrap(inter);
        if( wrapper != null ){
            client.put("wrapper", wrapper.get("value"));
        } else {
            client.put("wrapper", CatDefaultConfiguration.wrapper);
        }
        return client;
    }
    
    
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
 


    
    
    public String getBeanName() {
        return beanName;
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
    public CatClientFactory getFactory() {
        return factory;
    }
    public Class getFallback() {
        return fallback;
    }
    public boolean isFallbackMod() {
        return fallbackMod;
    }
    public Class<? extends ResponesWrapper> getWrapper() {
        return wrapper;
    }

}
