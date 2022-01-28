package cc.bugcat.catclient.beanInfos;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatMethodInterceptor;
import cc.bugcat.catclient.handler.CatLogsMod;
import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catface.utils.CatToosUtil;

import java.util.Map;
import java.util.Properties;

/**
 * 注解信息，单例
 * {@link CatClient}
 *
 * @author bugcat
 * */
public final class CatClientInfo {

    private final CatClientConfiguration clientConfig;

    private final String serviceName; //interface类名

    private final String host;  //远程服务器主机

    private final int connect;  //连接超时
    private final int socket;   //读取超时

    private final CatLogsMod logsMod; //日志记录方案

    private final String[] tags;    //api标签归类

    private final Class<? extends CatClientFactory> factoryClass;   //http发送工厂类

    private final Class<? extends CatMethodInterceptor> interceptorClass;   // 拦截器

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




    private CatClientInfo(CatClient client, Map<String, Object> paramMap, Properties envProp){

        //全局默认配置
        this.clientConfig = (CatClientConfiguration) paramMap.get("catClientConfiguration");

        this.serviceName = (String) paramMap.get("serviceName");

        String host = client.host();
        this.host = envProp.getProperty(host);

        int connect = client.connect();
        connect = connect < 0 ? -1 : connect;
        this.connect = CatClientConfiguration.connect == connect ? clientConfig.connect() : connect;

        int socket = client.socket();
        socket = socket < 0 ? -1 : socket;
        this.socket = CatClientConfiguration.socket == socket ? clientConfig.socket() : socket;

        CatLogsMod logsMod = client.logsMod();
        logsMod = CatClientConfiguration.logsMod.equals(logsMod) ? clientConfig.logsMod() : logsMod;
        this.logsMod = CatLogsMod.Def.equals(logsMod) ? CatLogsMod.All2 : logsMod;

        this.tags = client.tags();

        Class<? extends CatClientFactory> factoryClass = client.factory();
        this.factoryClass = CatClientConfiguration.factory.equals(factoryClass) ? clientConfig.clientFactory() : factoryClass;

        Class<? extends CatMethodInterceptor> interceptorClass = client.interceptor();
        this.interceptorClass = CatClientConfiguration.methodInterceptor.equals(interceptorClass) ? clientConfig.methodInterceptor() : interceptorClass;

        this.fallback = client.fallback();
        this.fallbackMod = fallback != Object.class;

        //响应包装器类，如果是ResponesWrapper.default，代表没有设置
        CatResponesWrapper responesWrapper = (CatResponesWrapper) paramMap.get("wrapper");
        Class<? extends AbstractResponesWrapper> wrapper = responesWrapper == null ? clientConfig.wrapper() : responesWrapper.value();
        this.wrapper = wrapper == CatClientConfiguration.wrapper ? null : wrapper;

        this.catface = (Catface) paramMap.get("catface");
    }


    /**
     * 构建CatClientInfo对象
     * @param interfaceClass     interface
     * @param catClient     不一定是interface上的注解
     * @param clientConfig        全局默认配置
     * @param envProp       环境变量
     * */
    public final static CatClientInfo build(Class interfaceClass, CatClient catClient, CatClientConfiguration clientConfig, Properties envProp) {
        Map<String, Object> paramMap = CatToosUtil.getAttributes(interfaceClass);
        paramMap.put("serviceName", interfaceClass.getSimpleName());
        paramMap.put("catClientConfiguration", clientConfig);
        CatClientInfo clientInfo = new CatClientInfo(catClient, paramMap, envProp);
        return clientInfo;
    }


    public CatClientConfiguration getClientConfig() {
        return clientConfig;
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
    public CatLogsMod getLogsMod() {
        return logsMod;
    }
    public String[] getTags() {
        return tags;
    }
    public Class<? extends CatClientFactory> getFactoryClass() {
        return factoryClass;
    }
    public Class<? extends CatMethodInterceptor> getInterceptorClass() {
        return interceptorClass;
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
