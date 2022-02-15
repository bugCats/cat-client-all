package cc.bugcat.catclient.config;

import cc.bugcat.catclient.annotation.EnableCatClient;
import cc.bugcat.catclient.handler.CatJacksonResolver;
import cc.bugcat.catclient.handler.CatLogsMod;
import cc.bugcat.catclient.spi.*;
import cc.bugcat.catclient.utils.CatRestHttp;
import cc.bugcat.catface.spi.AbstractResponesWrapper;



/**
 * 全局默认值
 *
 * {@link EnableCatClient#defaults()}
 *
 * @author bugcat
 * */
public class CatClientConfiguration {

    // 初始值
    public static final Class wrapper = AbstractResponesWrapper.Default.class;
    public static final Class factory = CatClientFactory.class;
    public static final Class methodInterceptor = CatMethodSendInterceptor.class;
    public static final CatLogsMod logsMod = CatLogsMod.Def;
    public static final int socket = 0;
    public static final int connect = 0;


    /**
     * 统一的响应实体包装器类
     * */
    public Class<? extends AbstractResponesWrapper> wrapper(){
        return wrapper;
    }


    /**
     * 读值超时，默认 1s；-1 代表不限制
     * */
    public int socket(){
        return 1000;
    }


    /**
     * 读值超时，默认 1s；-1 代表不限制
     * */
    public int connect(){
        return 1000;
    }


    /**
     * 默认日志记录方案
     * */
    public CatLogsMod logsMod(){
        return CatLogsMod.All2;
    }


    /**
     * 发送类和响应处理类工厂
     * */
    public Class<? extends CatClientFactory> clientFactory(){
        return factory;
    }

    /**
     * 默认的拦截器
     * */
    public Class<? extends CatMethodSendInterceptor> methodInterceptor(){
        return methodInterceptor;
    }

    /**
     * 默认http类
     * */
    public CatHttp catHttp(){
        return new CatRestHttp();
    }


    /**
     * 默认序列化对象
     * */
    public CatJsonResolver jsonResolver(){
        return new CatJacksonResolver();
    }


    /**
     * 默认日志打印
     * */
    public CatLoggerProcessor loggerProcessor(){
        return new CatLoggerProcessor(){};
    }

}
