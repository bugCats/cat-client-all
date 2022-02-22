package cc.bugcat.catserver.beanInfos;

import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.annotation.CatServer;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.spi.CatServerInterceptor;

import java.util.Arrays;
import java.util.Map;


/**
 * {@code @CatServer} 注解对象信息
 *
 * @author bugcat
 * */
public class CatServerInfo {


    /**
     * 全局配置
     * */
    private CatServerConfiguration serverConfig;

    /**
     * 被标记的CatServer类
     * */
    private final Class<?> serverClass;

    /**
     * api标签归类
     * */
    private final String[] tags;

    /**
     * controller的拦截器
     * */
    private final Class<? extends CatServerInterceptor>[] interceptors;

    /**
     * 响应包装器类处理{@link AbstractResponesWrapper}
     * */
    private final AbstractResponesWrapper wrapperHandler;

    /**
     * 是否启用精简模式
     * */
    private final Catface catface;
    private final boolean isCatface;



    private CatServerInfo(Class<?> serverClass, Map<String, Object> interfaceAttributes) {

        CatServer catServer = serverClass.getAnnotation(CatServer.class);

        this.serverConfig = (CatServerConfiguration) interfaceAttributes.get(CatToosUtil.INTERFACE_ATTRIBUTES_DEPENDS);

        this.serverClass = serverClass;

        this.tags = catServer.tags();

        this.interceptors = catServer.interceptors();

        //响应包装器类，如果是ResponesWrapper.default，代表没有设置
        CatResponesWrapper responesWrapper = (CatResponesWrapper) interfaceAttributes.get(CatToosUtil.INTERFACE_ATTRIBUTES_WRAPPER);
        if ( responesWrapper != null ){
            Class<? extends AbstractResponesWrapper> wrapper = CatToosUtil.comparator(CatServerConfiguration.WRAPPER, Arrays.asList(responesWrapper.value(), serverConfig.getWrapper()), null);
            this.wrapperHandler = wrapper != null ? AbstractResponesWrapper.getResponesWrapper(wrapper) : null;
        } else {
            this.wrapperHandler = null;
        }

        //是否启用精简模式
        this.catface = (Catface) interfaceAttributes.get(CatToosUtil.INTERFACE_ATTRIBUTES_CATFACE);
        this.isCatface = catface != null;

    }


    public final static CatServerInfo build(Class<?> serverClass, CatServerConfiguration serverConfig) {
        Map<String, Object> interfaceAttributes = CatToosUtil.getAttributes(serverClass);
        interfaceAttributes.put(CatToosUtil.INTERFACE_ATTRIBUTES_DEPENDS, serverConfig);
        CatServerInfo serverInfo = new CatServerInfo(serverClass, interfaceAttributes);
        return serverInfo;
    }


    public CatServerConfiguration getServerConfig() {
        return serverConfig;
    }
    public Class<?> getServerClass() {
        return serverClass;
    }
    public String[] getTags() {
        return tags;
    }
    public AbstractResponesWrapper getWrapperHandler() {
        return wrapperHandler;
    }
    public Class<? extends CatServerInterceptor>[] getInterceptors() {
        return interceptors;
    }
    public Catface getCatface() {
        return catface;
    }
    public boolean isCatface() {
        return isCatface;
    }
}
