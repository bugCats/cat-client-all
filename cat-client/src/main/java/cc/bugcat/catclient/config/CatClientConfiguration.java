package cc.bugcat.catclient.config;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.annotation.EnableCatClient;
import cc.bugcat.catclient.handler.CatJacksonResolver;
import cc.bugcat.catclient.handler.CatLogsMod;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatHttp;
import cc.bugcat.catclient.spi.CatPayloadResolver;
import cc.bugcat.catclient.spi.CatLoggerProcessor;
import cc.bugcat.catclient.spi.CatSendInterceptor;
import cc.bugcat.catclient.utils.CatRestHttp;
import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.catface.spi.AbstractResponesWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.function.Supplier;


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
    public static final Class<? extends CatSendInterceptor> METHOD_INTERCEPTOR = CatSendInterceptor.class;

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
    protected CatHttp globalCatHttp;
    
    @Autowired(required = false)
    protected CatPayloadResolver globalPayloadResolver;
    
    @Autowired(required = false)
    protected CatLoggerProcessor globalLoggerProcessor;

    @Autowired(required = false)
    protected ObjectMapper objectMapper;
    
    
    @Override
    public void afterPropertiesSet() {
        if( globalCatHttp == null ){
            globalCatHttp = new CatRestHttp();
        }
        if( globalPayloadResolver == null ){
            globalPayloadResolver = new CatJacksonResolver(objectMapper);
        }
        if( globalLoggerProcessor == null ){
            globalLoggerProcessor = new CatLoggerProcessor(){};
        }
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
     * 统一的响应实体包装器类。
     * 在客户端上可以添加{@link CatResponesWrapper}修改
     * */
    public Class<? extends AbstractResponesWrapper> getWrapper(){
        return WRAPPER;
    }


    /**
     * 默认的http发送拦截器
     * 在客户端可以通{@link CatClient#interceptor()}修改
     * */
    public Class<? extends CatSendInterceptor> getMethodInterceptor(){
        return METHOD_INTERCEPTOR;
    }
    
    
    /**
     * 发送类和响应处理类工厂。
     * 在客户端可以通{@link CatClient#factory()}修改
     * */
    public Class<? extends CatClientFactory> getClientFactory(){
        return CLIENT_FACTORY;
    }

    
    /**
     * 默认http类，建议为单例。
     * 可以在{@link CatClientFactory#getCatHttp()}中修改
     * */
    public CatHttp getCatHttp(){
        return globalCatHttp;
    }

    /**
     * 日志追踪id。可以结合ThreadLocal使用
     * */
    public Supplier<String> tracerProvide(){
        return () -> UUID.randomUUID().toString();
    }
    
    /**
     * 默认序列化对象，建议为单例。
     * 可以在{@link CatClientFactory#getPayloadResolver()}中修改
     * */
    public CatPayloadResolver getPayloadResolver(){
        return globalPayloadResolver;
    }


    /**
     * 默认日志打印，建议为单例。
     * 可以在{@link CatClientFactory#getLoggerProcessor()}中修改
     * */
    public CatLoggerProcessor getLoggerProcessor(){
        return globalLoggerProcessor;
    }


    
}
