package cc.bugcat.catclient.beanInfos;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.handler.CatClientDepend;
import cc.bugcat.catclient.handler.CatLogsMod;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatSendInterceptor;
import cc.bugcat.catface.annotation.CatNote;
import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.catface.handler.CatApiInfo;
import cc.bugcat.catface.handler.EnvironmentAdapter;
import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catface.utils.CatToosUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * interface上的{@link CatClient}注解描述信息
 *
 * @author bugcat
 * */
public final class CatClientInfo {

    
    /**
     * 组件前置依赖
     * */
    private final CatClientDepend clientDepend;

    /**
     * interface类名，默认首字母小写
     * */
    private final String serviceName;

    /**
     * 远程服务器主机：http://${host}/ctx
     * 此时${host}已经被变量填充
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
    private final Map<String, String> tagMap;

    /**
     * http发送工厂类
     * */
    private final Class<? extends CatClientFactory> factoryClass;

    /**
     * http拦截器
     * */
    private final Class<? extends CatSendInterceptor> interceptorClass;

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
    private final AbstractResponesWrapper wrapperHandler;


    /**
     * 是否使用精简模式
     * 如果interface上包含此注解，默认interface中所有方法均为api
     * {@link Catface}
     * */
    private final Catface catface;



    private CatClientInfo(CatClient client, CatClientApiInfo apiInfo){

        this.clientDepend = apiInfo.getDepend();
        this.serviceName = apiInfo.getServiceName();

        EnvironmentAdapter envProp = clientDepend.getEnvironment();
        CatClientConfiguration clientConfig = clientDepend.getClientConfig();

        String host = client.host();
        this.host = envProp.getProperty(host, String.class);

        int connect = client.connect();
        connect = connect < 0 ? -1 : connect;
        this.connect = CatToosUtil.comparator(CatClientConfiguration.CONNECT, Arrays.asList(connect), clientConfig.getConnect());

        int socket = client.socket();
        socket = socket < 0 ? -1 : socket;
        this.socket = CatToosUtil.comparator(CatClientConfiguration.SOCKET, Arrays.asList(socket), clientConfig.getSocket());

        this.logsMod = CatToosUtil.comparator(CatClientConfiguration.LOGS_MOD, Arrays.asList(client.logsMod(), clientConfig.getLogsMod()), CatLogsMod.All2);

        // 其他自定义参数、标记
        Map<String, String> tagMap = new HashMap<>();
        CatNote[] tags = client.tags();
        for ( CatNote tag : tags ) {
            String value = CatToosUtil.defaultIfBlank(tag.value(), "");
            //如果 key属性为空，默认赋值value
            String key = CatToosUtil.isBlank(tag.key()) ? value : tag.key();
            tagMap.put(key, envProp.getProperty(value, String.class));
        }
        this.tagMap = Collections.unmodifiableMap(tagMap);

        this.factoryClass = CatToosUtil.comparator(CatClientConfiguration.CLIENT_FACTORY, Arrays.asList(client.factory()), clientConfig.getClientFactory());

        this.interceptorClass = CatToosUtil.comparator(CatClientConfiguration.METHOD_INTERCEPTOR, Arrays.asList(client.interceptor()), clientConfig.getMethodInterceptor());

        //响应包装器类，如果是ResponesWrapper.default，代表没有设置
        CatResponesWrapper responesWrapper = apiInfo.getWrapper();
        if ( responesWrapper != null ){
            Class<? extends AbstractResponesWrapper> wrapper = CatToosUtil.comparator(CatClientConfiguration.WRAPPER, Arrays.asList(responesWrapper.value(), clientConfig.getWrapper()), null);
            this.wrapperHandler = wrapper != null ? AbstractResponesWrapper.getResponesWrapper(wrapper) : null;
        } else {
            this.wrapperHandler = null;
        }

        Class fallback = client.fallback();
        if ( CatClientConfiguration.FALLBACK_OFF.equals(fallback) ) { // Void.class 关闭回调
            this.fallback = Object.class;
            this.fallbackMod = false;
        } else {
            this.fallback = client.fallback();
            this.fallbackMod = true;
        }

        this.catface = apiInfo.getCatface();
    }


    /**
     * 构建CatClientInfo对象
     *
     * @param interfaceClass    interface
     * @param catClient         可以为null，不一定是interface上的注解。也可以是CatClients实例
     * @param depend            默认依赖
     * */
    public static CatClientInfo build(Class interfaceClass, CatClient catClient, CatClientDepend depend ) {
        if( catClient == null ){
            catClient = (CatClient) interfaceClass.getAnnotation(CatClient.class);
        }
        CatClientApiInfo apiInfo = CatToosUtil.getAttributes(interfaceClass, CatClientApiInfo::new);
        apiInfo.setServiceName(interfaceClass.getSimpleName());
        apiInfo.setDepend(depend);
        CatClientInfo clientInfo = new CatClientInfo(catClient, apiInfo);
        return clientInfo;
    }


    private static class CatClientApiInfo extends CatApiInfo {
        private String serviceName;
        private CatClientDepend depend;

        public String getServiceName() {
            return serviceName;
        }
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public CatClientDepend getDepend() {
            return depend;
        }
        public void setDepend(CatClientDepend depend) {
            this.depend = depend;
        }
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
    public Map<String, String> getTagMap() {
        return tagMap;
    }
    public Class<? extends CatClientFactory> getFactoryClass() {
        return factoryClass;
    }
    public Class<? extends CatSendInterceptor> getInterceptorClass() {
        return interceptorClass;
    }
    public boolean isFallbackMod() {
        return fallbackMod;
    }
    public Class getFallback() {
        return fallback;
    }
    public AbstractResponesWrapper getWrapperHandler() {
        return wrapperHandler;
    }
    public Catface getCatface() {
        return catface;
    }




}
