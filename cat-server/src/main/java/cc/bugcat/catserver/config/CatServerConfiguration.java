package cc.bugcat.catserver.config;

import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catserver.annotation.CatServer;
import cc.bugcat.catserver.handler.CatServerDefaults;
import cc.bugcat.catserver.handler.CatServerDefaults.*;
import cc.bugcat.catserver.spi.*;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 全局默认值
 *
 *
 * @author bugcat
 * */
public class CatServerConfiguration implements InitializingBean {


    public static final Class<? extends AbstractResponesWrapper> WRAPPER = AbstractResponesWrapper.Default.class;


    protected ConcurrentMap<Class, CatServerResultHandler> resultHandlerMap = new ConcurrentHashMap<>();

    protected CatServerResultHandler defaultResultHandler;
    protected CatInterceptor defaultGlobalInterceptor;
    protected List<CatInterceptorGroup> interceptorGroups;



    @Override
    public void afterPropertiesSet() {
        this.defaultResultHandler = new CatServerResultHandler(){};
        this.defaultGlobalInterceptor = CatServerDefaults.DEFAULT_INTERCEPTOR;
        this.interceptorGroups = new ArrayList<>(0);
    }


    /**
     * 统一的响应实体包装器类
     * */
    public Class<? extends AbstractResponesWrapper> getWrapper(){
        return WRAPPER;
    }



    /**
     * CatServer类响应处理
     * */
    public CatServerResultHandler getResultHandler(AbstractResponesWrapper wrapperHandler) {
        CatServerResultHandler resultHandler = null;
        if ( wrapperHandler != null ) {
            resultHandler = resultHandlerMap.get(wrapperHandler.getWrapperClass());
            if ( resultHandler == null ) {
                resultHandler = CatServerDefaults.newResultHandler(wrapperHandler);
                resultHandlerMap.putIfAbsent(wrapperHandler.getWrapperClass(), resultHandler);
            }
        } else {
            resultHandler = defaultResultHandler;
        }
        return resultHandler;
    }


    /**
     * 所有CatServer类共享的拦截器
     * 一般用于记录日志
     * */
    public CatInterceptor getGlobalInterceptor(){
        return defaultGlobalInterceptor;
    }


    /**
     * 其他拦截器组
     * 会替换{@link CatServer#interceptors()}此处配置的默认拦截
     * */
    public List<CatInterceptorGroup> getInterceptorGroup(){
        return interceptorGroups;
    }

}
