package cc.bugcat.catserver.config;

import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catserver.handler.CatServerDefaults;
import cc.bugcat.catserver.spi.CatInterceptorGroup;
import cc.bugcat.catserver.spi.CatResultHandler;
import cc.bugcat.catserver.spi.CatServerInterceptor;
import cc.bugcat.catserver.spi.SimpleWrapperResultHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

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
    @Autowired(required = false)
    @Qualifier("defaultInterceptor")
    protected CatServerInterceptor globalInterceptor;

    /**
     * 运行时拦截器组
     * */
    @Autowired(required = false)
    protected List<CatInterceptorGroup> globalInterceptorGroups;



    @Override
    public void afterPropertiesSet() {
        if( globalInterceptor == null ){
            this.globalInterceptor = CatServerDefaults.DEFAULT_INTERCEPTOR;
        }
        if( globalInterceptorGroups == null ){
            this.globalInterceptorGroups = new ArrayList<>(0);
        }
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
        return wrapperHandler != null ? SimpleWrapperResultHandler.class : CatResultHandler.Default.class;
    }
    


    /**
     * 全局默认的拦截器，用于替换@CatServer#interceptors()默认的拦截器对象。
     * 
     * 当@CatServer配置了自定义拦截器时，自定义拦截器会覆盖默认值。
     * 除非手动在加上默认拦截器：@CatServer(interceptors = {UserInterceptor.class, CatServerInterceptor.class})
     * 其中CatServerInterceptor.class具体实例，由此方法返回。
     * 
     * CatServerInterceptor.Off 关闭所有拦截器，包括运行时拦截器。
     * */
    public CatServerInterceptor getServerInterceptor(){
        return globalInterceptor;
    }


    /**
     * 全局拦截器组，在运行时匹配。
     * */
    public List<CatInterceptorGroup> getInterceptorGroup(){
        return globalInterceptorGroups;
    }

}
