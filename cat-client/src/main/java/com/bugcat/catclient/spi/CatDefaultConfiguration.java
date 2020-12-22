package com.bugcat.catclient.spi;

import com.bugcat.catclient.handler.RequestLogs;
import com.bugcat.catclient.handler.ResultProcessor;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.catclient.utils.CatClientUtil;
import com.bugcat.catface.spi.ResponesWrapper;


/**
 * 全局默认值
 * @author bugcat
 * */
public class CatDefaultConfiguration {
    
    public static final Class wrapper = ResponesWrapper.Default.class;
    public static final Integer socket = 0;
    public static final Integer connect = 0;
    public static final RequestLogs logs = RequestLogs.Def;


    private CatHttp http;
    private ResultProcessor resultHandler;
    
    
    public CatDefaultConfiguration(){}
    

    
    /**
     * 统一的响应实体类包裹对象
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
     * 默认http类
     * 单例
     * */
    public CatHttp catHttp(){
        if( http == null ){
            synchronized ( this ){
                if( http == null ){
                    http = CatClientUtil.getBean(CatHttp.class);
                }
            }
        }
        return http;
    }

    
    /**
     * 默认请求发送类
     * 多例
     * */
    public SendProcessor sendHandler(){
        return new DefaultSendHandler();
    }

    
    
    /**
     * 默认响应处理类
     * 单例
     * */
    public ResultProcessor resultHandler(){
        if( resultHandler == null ){
            synchronized ( this ){
                if( resultHandler == null ){
                    resultHandler = new DefaultResultHandler();
                }
            }
        }
        return resultHandler;
    }
    
    
}
