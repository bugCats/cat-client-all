package com.bugcat.catclient.spi;

import com.bugcat.catclient.handler.CatMethodInterceptor;
import com.bugcat.catclient.handler.RequestLogs;
import com.bugcat.catclient.utils.CatClientUtil;
import com.bugcat.catface.spi.ResponesWrapper;


/**
 * 全局默认值
 * @author bugcat
 * */
public class DefaultConfiguration {
    
    
    // 初始值
    public static final Class wrapper = ResponesWrapper.Default.class;
    public static final Class factory = CatClientFactory.class;
    public static final Class interceptor = DefaultMethodInterceptor.class;
    public static final RequestLogs logs = RequestLogs.Def;
    public static final int socket = 0;
    public static final int connect = 0;
    
    
    /**
     * 统一的响应实体包装器类
     * */
    public Class<? extends ResponesWrapper> wrapper(){
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
    public RequestLogs logs(){
        return RequestLogs.All2;
    }
    
    
    /**
     * 默认的http流程处理
     * */
    public Class<? extends CatMethodInterceptor> interceptor(){
        return interceptor;
    }


    /**
     * 发送类和响应处理类工厂
     * */
    public Class<? extends CatClientFactory> clientFactory(){
        return CatClientFactory.class;
    }

    

    /**
     * 默认http类
     * */
    public CatHttp catHttp(){
        return CatClientUtil.getBean(CatHttp.class);
    }
    

}
