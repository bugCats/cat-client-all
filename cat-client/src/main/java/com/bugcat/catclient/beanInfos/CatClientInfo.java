package com.bugcat.catclient.beanInfos;

import com.bugcat.catclient.annotation.CatClient;
import com.bugcat.catclient.handler.CatMethodInterceptor;
import com.bugcat.catclient.handler.RequestLogs;
import com.bugcat.catclient.spi.CatClientFactory;
import com.bugcat.catclient.spi.DefaultConfiguration;
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
    
    private Class<? extends CatMethodInterceptor> interceptor;      //动态代理拦截器类
    
    private Class fallback;                 //http异常处理类
    private boolean fallbackMod = false;    //是否启用了fallback模式
    
    private Class<? extends ResponesWrapper> wrapper;      //响应包裹类
    
    
    private CatClientInfo(AnnotationAttributes attr, Properties prop){

        DefaultConfiguration config = CatClientUtil.getBean(DefaultConfiguration.class);
        
        this.beanName = attr.getString("value");
        
        String host = attr.getString("host");
        this.host = prop.getProperty(host);
        
        int connect = attr.getNumber("connect");
        this.connect = connect < 0 ? -1 : connect;
        this.connect = DefaultConfiguration.connect.equals(this.connect) ? config.connect() : this.connect;

        int socket = attr.getNumber("socket");
        this.socket = socket < 0 ? -1 : socket;
        this.socket = DefaultConfiguration.socket.equals(this.socket) ? config.socket() : this.socket;

        
        RequestLogs logs = attr.getEnum("logs");
        logs = DefaultConfiguration.logs.equals(logs) ? config.logs() : logs;
        this.logs = RequestLogs.Def.equals(logs) ? RequestLogs.All2 : logs;
        
        
        Class<? extends CatClientFactory> factory = attr.getClass("factory");
        try {
            this.factory = factory.newInstance(); 
        } catch ( Exception e ) {
            this.factory = new CatClientFactory();
            System.err.println("初始化=" + factory.getSimpleName() + "异常！使用默认工厂！");
        }
        this.factory.setDefaultConfiguration(config);


        Class<? extends CatMethodInterceptor> interceptor = attr.getClass("interceptor");
        this.interceptor = DefaultConfiguration.interceptor.equals(interceptor) ? config.interceptor() : interceptor;
        

        this.fallback = attr.getClass("fallback");
        this.fallbackMod = fallback != Object.class;
        
        
        //响应包裹类，如果是ResponesWrapper.default，代表没有设置
        Class<? extends ResponesWrapper> wrapper = attr.getClass("wrapper");
        wrapper = DefaultConfiguration.wrapper.equals(wrapper) ? config.wrapper() : wrapper;
        this.wrapper = wrapper == DefaultConfiguration.wrapper ? null : wrapper;
        
    }
    
    
    
    public final static CatClientInfo buildClientInfo(Class inter, Properties prop) {
        AnnotationAttributes attributes = getAttributes(inter);
        CatClientInfo clientInfo = new CatClientInfo(attributes, prop);
        return clientInfo;
    }
    
    
    private static AnnotationAttributes getAttributes(Class inter) {
        StandardAnnotationMetadata metadata = new StandardAnnotationMetadata(inter);
        AnnotationAttributes client = new AnnotationAttributes(metadata.getAnnotationAttributes(CatClient.class.getName()));
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
    public Class<? extends CatMethodInterceptor> getInterceptor() {
        return interceptor;
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
