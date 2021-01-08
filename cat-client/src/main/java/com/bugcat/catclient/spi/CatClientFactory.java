package com.bugcat.catclient.spi;


import com.bugcat.catclient.handler.ResultProcessor;
import com.bugcat.catclient.handler.SendProcessor;

/**
 * 处理器，可以设置请求发送类、结果响应类
 * 每个@CatCilent类对应一个CatClientFactory
 * @author bugcat
 * */
public class CatClientFactory {
    
    
    private DefaultConfiguration config;
    
    
    
    /**
     * http 类
     * 单例
     * */
    public final CatHttp getCatHttp(){
        return catHttp();
    }
    
    
    
    /**
     * 如果在定义请求方法时，没有传入请求发送类，则在代理类中，自动生成一个请求发送类对象
     * 多例
     * */
    public final SendProcessor getSendHandler(){
        return sendHandler();
    }
    
    
    
    /**
     * 获取结果处理类
     * 单例
     * */
    public final ResultProcessor getResultHandler(){
        return resultHandler();
    }

    
    
    /**
     * 设置默认值对象
     * */
    public final void setDefaultConfiguration(DefaultConfiguration config){
        this.config = config;
    }
    
    
    
    
    /**
     * 提供给子类重写
     * 通过子类重写方式，返回指定的http类
     * 也可以通过@Bean、@Component等形成，注册到Spring容器。此处通过Spring容器获取http类
     * 最后也可以通过CatToosUtil.setDefaultCatHttp
     * 优先度逐渐降低
     * */
    protected CatHttp catHttp(){
        return config.catHttp();
    }
    
    
    /**
     * 得到http发送类类型
     * 提供给子类重写
     * */
    protected SendProcessor sendHandler(){
        return config.sendHandler();
    }
    
    
    /**
     * 得到结果处理类类型
     * 提供给子类重写
     * */
    protected ResultProcessor resultHandler(){
        return config.resultHandler();
    }
    
    

}
