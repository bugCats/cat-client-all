package cc.bugcat.catserver.beanInfos;

import cc.bugcat.catface.annotation.CatNote;
import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.annotation.CatServer;
import cc.bugcat.catserver.asm.CatEnhancerDepend;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.spi.CatResultHandler;
import cc.bugcat.catserver.spi.CatServerInterceptor;

import java.util.*;


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
    private final Map<String, String> tagMap;

    /**
     * controller的拦截器
     * */
    private final Set<Class<? extends CatServerInterceptor>> interceptors;
    
    /**
     * 响应包装器类处理{@link AbstractResponesWrapper}
     * */
    private final AbstractResponesWrapper wrapperHandler;

    /**
     * controller响应处理，一般是异常流程
     * */
    private final Class<? extends CatResultHandler> resultHandler;

    /**
     * 是否启用精简模式
     * */
    private final Catface catface;
    private final boolean isCatface;



    private CatServerInfo(Class<?> serverClass, Map<String, Object> interfaceAttributes, Properties envProp) {

        CatServer catServer = serverClass.getAnnotation(CatServer.class);

        this.serverConfig = (CatServerConfiguration) interfaceAttributes.get(CatToosUtil.INTERFACE_ATTRIBUTES_DEPENDS);

        this.serverClass = serverClass;

        // 其他自定义参数、标记
        Map<String, String> tagMap = new HashMap<>();
        CatNote[] tags = catServer.tags();
        for ( CatNote tag : tags ) {
            String value = CatToosUtil.defaultIfBlank(tag.value(), "");
            //如果 key属性为空，默认赋值value
            String key = CatToosUtil.isBlank(tag.key()) ? value : tag.key();
            if ( value.startsWith("${") ) {
                tagMap.put(key, envProp.getProperty(value));
            } else {
                tagMap.put(key, value);
            }
        }
        this.tagMap = Collections.unmodifiableMap(tagMap);

        Set<Class<? extends CatServerInterceptor>> interceptors = new LinkedHashSet<>(catServer.interceptors().length * 2);
        //默认情况在所有拦截器之前执行
        interceptors.add(CatServerInterceptor.Group.class); 
        for ( Class<? extends CatServerInterceptor> interceptor : catServer.interceptors() ) {
            if( interceptors.contains(interceptor) ){
                interceptors.remove(interceptor);
            }
            interceptors.add(interceptor);
        }
        this.interceptors = interceptors;

        //响应包装器类，如果是ResponesWrapper.default，代表没有设置
        CatResponesWrapper responesWrapper = (CatResponesWrapper) interfaceAttributes.get(CatToosUtil.INTERFACE_ATTRIBUTES_WRAPPER);
        if ( responesWrapper != null ){
            Class<? extends AbstractResponesWrapper> wrapper = CatToosUtil.comparator(CatServerConfiguration.WRAPPER, Arrays.asList(responesWrapper.value(), serverConfig.getWrapper()), null);
            this.wrapperHandler = wrapper != null ? AbstractResponesWrapper.getResponesWrapper(wrapper) : null;
        } else {
            this.wrapperHandler = null;
        }

        this.resultHandler = CatToosUtil.comparator(CatServerConfiguration.RESULT_HANDLER, Arrays.asList(catServer.resultHandler()), serverConfig.getResultHandler(wrapperHandler));


        //是否启用精简模式
        this.catface = (Catface) interfaceAttributes.get(CatToosUtil.INTERFACE_ATTRIBUTES_CATFACE);
        this.isCatface = catface != null;

    }


    public final static CatServerInfo build(Class<?> serverClass, CatEnhancerDepend enhancerDepend) {
        Map<String, Object> interfaceAttributes = CatToosUtil.getAttributes(serverClass);
        interfaceAttributes.put(CatToosUtil.INTERFACE_ATTRIBUTES_DEPENDS, enhancerDepend.getServerConfig());
        CatServerInfo serverInfo = new CatServerInfo(serverClass, interfaceAttributes, enhancerDepend.getEnvProp());
        return serverInfo;
    }


    
    public CatServerConfiguration getServerConfig() {
        return serverConfig;
    }
    public Class<?> getServerClass() {
        return serverClass;
    }
    public Map<String, String> getTagMap() {
        return tagMap;
    }
    public AbstractResponesWrapper getWrapperHandler() {
        return wrapperHandler;
    }
    public Set<Class<? extends CatServerInterceptor>> getInterceptors() {
        return interceptors;
    }
    public Class<? extends CatResultHandler> getResultHandler() {
        return resultHandler;
    }
    public Catface getCatface() {
        return catface;
    }
    public boolean isCatface() {
        return isCatface;
    }
    
}
