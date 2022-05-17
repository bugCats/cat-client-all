package cc.bugcat.catserver.config;

import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catserver.handler.CatServerDefaults;
import cc.bugcat.catserver.spi.CatInterceptorGroup;
import cc.bugcat.catserver.spi.CatServerInterceptor;
import cc.bugcat.catserver.spi.CatResultHandler;
import cc.bugcat.catserver.spi.DefaultWrapperResultHandler;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局默认值
 *
 * @author bugcat
 * */
public class CatServerConfiguration implements InitializingBean {


    /**
     * 包装器类
     * */
    public static final Class<? extends AbstractResponesWrapper> WRAPPER = AbstractResponesWrapper.Default.class;

    /**
     * 响应处理器
     * */
    public static final Class<? extends CatResultHandler> RESULT_HANDLER = CatResultHandler.class;
    
    /**
     * 全局拦截器
     * */
    protected CatServerInterceptor defaultGlobalInterceptor;

    /**
     * 运行时拦截器组
     * */
    protected List<CatInterceptorGroup> interceptorGroups;



    @Override
    public void afterPropertiesSet() {
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
     * 默认的响应处理
     * */
    public Class<? extends CatResultHandler> getResultHandler(AbstractResponesWrapper wrapperHandler){
        return wrapperHandler != null ? DefaultWrapperResultHandler.class : CatResultHandler.Default.class;
    }
    


    /**
     * 全局默认的拦截器，用于替换@CatServer#interceptors()默认拦截器对象，一般用于记录日志。
     * 后续配置@CatServer拦截器时，defaultGlobalInterceptor会替换CatServerInterceptor.class的位置。
     *
     * @CatServer(interceptors = {CatServerInterceptor.class, UserInterceptor.class})
     *
     * 实际会执行 defaultGlobalInterceptor -> userInterceptor 2个拦截器。
     * 
     * CatServerInterceptor.Off 关闭所有拦截器，包括运行时拦截器。
     * */
    public CatServerInterceptor getGlobalInterceptor(){
        return defaultGlobalInterceptor;
    }


    /**
     * 全局拦截器组，在运行时匹配。
     * 默认在自定义拦截之前执行，也可以使用 CatServerInterceptor.GROUP.class 手动调整拦截器位置。
     * */
    public List<CatInterceptorGroup> getInterceptorGroup(){
        return interceptorGroups;
    }

}
