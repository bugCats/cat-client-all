package cc.bugcat.catserver.handler;

import cc.bugcat.catface.annotation.CatNote;
import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.catface.handler.CatApiInfo;
import cc.bugcat.catface.handler.EnvironmentAdapter;
import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.annotation.CatServer;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.spi.CatResultHandler;
import cc.bugcat.catserver.spi.CatServerInterceptor;
import cc.bugcat.catserver.utils.CatServerUtil;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * {@code @CatServer} 注解对象信息
 *
 * @author bugcat
 * */
public class CatServerInfo {

    /**
     * 组件前置依赖
     * */
    private final CatServerDepend serverDepend;
    
    /**
     * 被标记的CatServer类
     * */
    private final Class<?> serverClass;
    
    /**
     * api标签归类
     * */
    private final Map<String, String> tagsMap;

    /**
     * controller的拦截器
     * */
    private final Class<? extends CatServerInterceptor>[] interceptors;
    
    /**
     * 响应包装器类处理{@link AbstractResponesWrapper}
     * */
    private final AbstractResponesWrapper wrapperHandler;

    /**
     * controller响应处理，一般是异常流程
     * */
    private final CatResultHandler resultHandler;

    /**
     * 是否启用精简模式
     * */
    private final Catface catface;
    private final boolean isCatface;

    /**
     * 在interface上的RequestMapping注解
     * */
    private final String basePath;



    private CatServerInfo(CatServer catServer, CatServerApiInfo apiInfo) {
        
        this.serverClass = apiInfo.getServerClass();
        this.serverDepend = apiInfo.getServerDepend();
        
        CatServerConfiguration serverConfig = serverDepend.getServerConfig();
        EnvironmentAdapter envProp = serverDepend.getEnvironmentAdapter();

        // 其他自定义参数、标记
        Map<String, String> tagsMap = new HashMap<>();
        CatNote[] tags = catServer.tags();
        for ( CatNote tag : tags ) {
            String value = CatToosUtil.defaultIfBlank(tag.value(), "");
            //如果 key属性为空，默认赋值value
            String key = CatToosUtil.isBlank(tag.key()) ? value : tag.key();
            
            String tagsNote = envProp.getProperty(value, String.class);
            tagsMap.put(key, tagsNote);
        }
        this.tagsMap = Collections.unmodifiableMap(tagsMap);

        // 自定义拦截器
        this.interceptors = catServer.interceptors();

        // 响应包装器类，如果是ResponesWrapper.default，代表没有设置
        AbstractResponesWrapper wrapperHandler = null;
        CatResponesWrapper responesWrapper = apiInfo.getWrapper();
        if ( responesWrapper != null ){
            Class<? extends AbstractResponesWrapper> wrapperClass = CatToosUtil.comparator(CatServerConfiguration.WRAPPER, Arrays.asList(responesWrapper.value(), serverConfig.getWrapper()), null);
            if( wrapperClass != null ){
                wrapperHandler = CatServerUtil.getBean(wrapperClass);
                if( wrapperHandler == null  ){
                    wrapperHandler = AbstractResponesWrapper.getResponesWrapper(wrapperClass, handler -> {
                        CatServerUtil.registerBean(wrapperClass, handler);
                    });
                }
            }
        }
        this.wrapperHandler = wrapperHandler;

        
        Class<? extends CatResultHandler> resultHandlerClass = CatToosUtil.comparator(CatServerConfiguration.RESULT_HANDLER, Arrays.asList(catServer.resultHandler()), serverConfig.getResultHandler(wrapperHandler));
        this.resultHandler = CatServerUtil.getBean(resultHandlerClass);
        this.resultHandler.setResponesWrapper(this.wrapperHandler);
        
        
        // 是否启用精简模式
        this.catface = apiInfo.getCatface();
        this.isCatface = catface != null;

        if( isCatface ){
            this.basePath = "";
        } else {
            this.basePath = envProp.getProperty(apiInfo.getBasePath(), "");
        }
    }


    public final static CatServerInfo build(Class<?> serverClass, CatServerDepend serverDepend) {
        CatServer catServer = serverClass.getAnnotation(CatServer.class);
        CatServerApiInfo apiInfo = new CatServerApiInfo();
        apiInfo.setServerClass(serverClass);
        apiInfo.setServerDepend(serverDepend);
        CatToosUtil.parseInterfaceAttributes(serverClass, apiInfo);
        CatServerInfo serverInfo = new CatServerInfo(catServer, apiInfo);
        return serverInfo;
    }
    
    
    private static class CatServerApiInfo extends CatApiInfo {
        private Class serverClass;
        private CatServerDepend serverDepend;

        public Class getServerClass() {
            return serverClass;
        }
        public void setServerClass(Class serverClass) {
            this.serverClass = serverClass;
        }

        public CatServerDepend getServerDepend() {
            return serverDepend;
        }
        public void setServerDepend(CatServerDepend serverDepend) {
            this.serverDepend = serverDepend;
        }
    }

    public CatServerDepend getServerDepend() {
        return serverDepend;
    }
    public Class<?> getServerClass() {
        return serverClass;
    }
    public Map<String, String> getTagsMap() {
        return tagsMap;
    }
    public AbstractResponesWrapper getWrapperHandler() {
        return wrapperHandler;
    }
    public Class<? extends CatServerInterceptor>[] getInterceptors() {
        return interceptors;
    }
    public CatResultHandler getResultHandler() {
        return resultHandler;
    }
    public Catface getCatface() {
        return catface;
    }
    public boolean isCatface() {
        return isCatface;
    }
    public String getBasePath() {
        return basePath;
    }
}
