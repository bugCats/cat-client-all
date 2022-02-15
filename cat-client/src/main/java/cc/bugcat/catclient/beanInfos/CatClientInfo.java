package cc.bugcat.catclient.beanInfos;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.annotation.EnableCatClient;
import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatMethodSendInterceptor;
import cc.bugcat.catclient.handler.CatLogsMod;
import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catface.utils.CatToosUtil;

import java.util.Map;
import java.util.Properties;

/**
 * interface上的{@link CatClient}注解描述信息
 *
 * @author bugcat
 * */
public final class CatClientInfo {

    /**
     * 全局默认配置类
     * 可以在{@link EnableCatClient}注解的{@code defaults}属性指定
     * */
    private final CatClientDepend clientDepend;

    /**
     * interface类名
     * */
    private final String serviceName;

    /**
     * 远程服务器主机：http://${host}/ctx，此时${host}已经被变量填充
     * */
    private final String host;

    /**
     * 连接超时
     * */
    private final int connect;

    /**
     * 读取超时
     * */
    private final int socket;

    /**
     * 日志记录方案
     * */
    private final CatLogsMod logsMod;

    /**
     * api标签归类
     * */
    private final String[] tags;

    /**
     * http发送工厂类
     * */
    private final Class<? extends CatClientFactory> factoryClass;

    /**
     * http拦截器
     * */
    private final Class<? extends CatMethodSendInterceptor> interceptorClass;

    /**
     * 是否启用了fallback模式
     * */
    private final boolean fallbackMod;

    /**
     * 回调类
     * */
    private final Class fallback;


    /**
     * 响应包装器类处理{@link AbstractResponesWrapper}
     * */
    private final Class<? extends AbstractResponesWrapper> wrapper;


    /**
     * 是否使用精简模式
     * 如果interface上包含此注解，默认interface中所有方法均为api
     * {@link Catface}
     * */
    private final Catface catface;




    private CatClientInfo(CatClient client, Map<String, Object> interfaceAttributes, Properties envProp){

        this.clientDepend = (CatClientDepend) interfaceAttributes.get(CatToosUtil.INTERFACE_ATTRIBUTES_CLIENT_DEPENDS);
        this.serviceName = (String) interfaceAttributes.get(CatToosUtil.INTERFACE_ATTRIBUTES_SERVICE_NAME);

        CatClientConfiguration clientConfig = clientDepend.getClientConfig();

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

        Class<? extends CatMethodSendInterceptor> interceptorClass = client.interceptor();
        this.interceptorClass = CatClientConfiguration.methodInterceptor.equals(interceptorClass) ? clientConfig.methodInterceptor() : interceptorClass;

        this.fallback = client.fallback();
        this.fallbackMod = fallback != Object.class;

        //响应包装器类，如果是ResponesWrapper.default，代表没有设置
        CatResponesWrapper responesWrapper = (CatResponesWrapper) interfaceAttributes.get(CatToosUtil.INTERFACE_ATTRIBUTES_WRAPPER);
        Class<? extends AbstractResponesWrapper> wrapper = responesWrapper == null ? clientConfig.wrapper() : responesWrapper.value();
        this.wrapper = CatClientConfiguration.wrapper.equals(wrapper) ? null : wrapper;

        this.catface = (Catface) interfaceAttributes.get(CatToosUtil.INTERFACE_ATTRIBUTES_CATFACE);
    }


    /**
     * 构建CatClientInfo对象
     *
     * @param interfaceClass    interface
     * @param catClient         可以为null，不一定是interface上的注解
     * @param depends           默认依赖
     * @param envProp           环境变量
     * */
    public final static CatClientInfo build(Class interfaceClass, CatClient catClient, CatClientDepend depends, Properties envProp) {
        if( catClient == null ){
            catClient = (CatClient) interfaceClass.getAnnotation(CatClient.class);
        }
        Map<String, Object> interfaceAttributes = CatToosUtil.getAttributes(interfaceClass);
        interfaceAttributes.put(CatToosUtil.INTERFACE_ATTRIBUTES_SERVICE_NAME, interfaceClass.getSimpleName());
        interfaceAttributes.put(CatToosUtil.INTERFACE_ATTRIBUTES_CLIENT_DEPENDS, depends);
        CatClientInfo clientInfo = new CatClientInfo(catClient, interfaceAttributes, envProp);
        return clientInfo;
    }


    public CatClientDepend getClientDepend() {
        return clientDepend;
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
    public Class<? extends CatMethodSendInterceptor> getInterceptorClass() {
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
