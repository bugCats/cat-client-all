package cc.bugcat.catclient.config;

import cc.bugcat.catclient.annotation.EnableCatClient;
import cc.bugcat.catclient.handler.CatJacksonResolver;
import cc.bugcat.catclient.handler.CatLogsMod;
import cc.bugcat.catclient.spi.*;
import cc.bugcat.catclient.utils.CatClientUtil;
import cc.bugcat.catclient.utils.CatRestHttp;
import cc.bugcat.catface.spi.AbstractResponesWrapper;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;


/**
 * 全局默认值
 *
 * 在{@link EnableCatClient#configuration()}配置
 *
 * @author bugcat
 * */
public class CatClientConfiguration implements InitializingBean {

    /**
     * 包装器类
     * */
    public static final Class<? extends AbstractResponesWrapper> WRAPPER = AbstractResponesWrapper.Default.class;

    /**
     * 默认工厂类
     * */
    public static final Class<? extends CatClientFactory> CLIENT_FACTORY = CatClientFactory.class;

    /**
     * 默认http拦截器类
     * */
    public static final Class<? extends CatMethodSendInterceptor> METHOD_INTERCEPTOR = CatMethodSendInterceptor.class;

    /**
     * 默认打印日志方案
     * */
    public static final CatLogsMod LOGS_MOD = CatLogsMod.Def;

    /**
     * 关闭异常回调模式
     * */
    public static final Class FALLBACK_OFF = Void.class;

    /**
     * http链接读取超时
     * */
    public static final int SOCKET = 0;
    public static final int CONNECT = 0;


    @Autowired(required = false)
    protected CatHttp defaultCatHttp;
    
    @Autowired(required = false)
    protected CatJsonResolver defaultJsonResolver;
    
    @Autowired(required = false)
    protected CatLoggerProcessor defaultLoggerProcessor;

    
    
    @Override
    public void afterPropertiesSet() {
        if( defaultCatHttp == null ){
            defaultCatHttp = new CatRestHttp();
        }
        if( defaultJsonResolver == null ){
            defaultJsonResolver = new CatJacksonResolver();
        }
        if( defaultLoggerProcessor == null ){
            defaultLoggerProcessor = new CatLoggerProcessor(){};
        }
    }


    /**
     * 统一的响应实体包装器类
     * */
    public Class<? extends AbstractResponesWrapper> getWrapper(){
        return WRAPPER;
    }


    /**
     * http读值超时毫秒，默认 1s；-1 代表不限制
     * */
    public int getSocket(){
        return 1000;
    }


    /**
     * http链接超时毫秒，默认 1s；-1 代表不限制
     * */
    public int getConnect(){
        return 1000;
    }


    /**
     * 默认日志记录方案
     * */
    public CatLogsMod getLogsMod(){
        return CatLogsMod.All2;
    }


    /**
     * 发送类和响应处理类工厂
     * */
    public Class<? extends CatClientFactory> getClientFactory(){
        return CLIENT_FACTORY;
    }


    /**
     * 默认的http发送拦截器
     * */
    public Class<? extends CatMethodSendInterceptor> getMethodInterceptor(){
        return METHOD_INTERCEPTOR;
    }

    /**
     * 默认http类
     * 建议为单例
     * */
    public CatHttp getCatHttp(){
        return defaultCatHttp;
    }


    /**
     * 默认序列化对象
     * 建议为单例
     * */
    public CatJsonResolver getJsonResolver(){
        return defaultJsonResolver;
    }


    /**
     * 默认日志打印
     * 建议为单例
     * */
    public CatLoggerProcessor getLoggerProcessor(){
        return defaultLoggerProcessor;
    }

}
