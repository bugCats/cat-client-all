package com.bugcat.catclient.spi;


import com.bugcat.catclient.handler.AbstractResultProcessor;
import com.bugcat.catclient.handler.SendProcessor;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;
import java.util.function.Supplier;


/**
 * 工厂，设置请求发送类、结果响应类
 * 
 * @author bugcat
 * */
@Component
public class CatClientFactory {
    
    
    private DefaultConfiguration configuration;
    
    private AbstractResultProcessor resultHandler;
    private CatHttp http;
    private CatJsonResolver jsonResolver;
    

    
    public final CatClientFactory configuration(DefaultConfiguration configuration){
        synchronizSetValueIfNull(this.configuration, () -> configuration, CatClientFactory::setConfiguration);
        return this;
    }
    
    
    
    /**
     * http 类
     * 单例
     * */
    public final CatHttp getCatHttp(){
        synchronizSetValueIfNull(this.http, () -> catHttp(), CatClientFactory::setHttp);
        return http;
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
    public final AbstractResultProcessor getResultHandler(){
        synchronizSetValueIfNull(this.resultHandler, () -> resultHandler(), CatClientFactory::setResultHandler);
        return resultHandler;
    }



    /**
     * 获取对象序列化处理类
     * 单例
     * */
    public final CatJsonResolver getJsonResolver(){
        synchronizSetValueIfNull(this.jsonResolver, () -> jsonResolver(), CatClientFactory::setJsonResolver);
        return jsonResolver;
    }






    /**
     * 提供给子类重写
     * 通过子类重写方式，返回指定的http类
     * 可以修改DefaultConfiguration#catHttp方法返回值
     * 也可以通过@Bean、@Component等形成，注册到Spring容器
     * 优先度逐渐降低
     * */
    protected CatHttp catHttp(){
        return getConfiguration().catHttp();
    }
    
    
    /**
     * 默认请求发送类
     * 提供给子类重写
     * */
    protected SendProcessor sendHandler(){
        return new DefaultSendHandler();
    }
    
    
    /**
     * 默认响应处理类
     * 提供给子类重写
     * */
    protected AbstractResultProcessor resultHandler(){
        return new DefaultResultHandler();
    }

    
    /**
     * 默认的对象序列化工具，为fastjson
     * 提供给子类重写
     * */
    protected CatJsonResolver jsonResolver(){
        return getConfiguration().jsonResolver();
    }
    
    
    
    private final <T> void synchronizSetValueIfNull(T value, Supplier<T> supplier, BiConsumer<CatClientFactory, T> consumer){
        if( value == null ){
            synchronized ( this ) {
                if ( value == null ) {
                    consumer.accept(this, supplier.get());
                }
            }
        }
    }

    
    private void setConfiguration(DefaultConfiguration configuration) {
        this.configuration = configuration;
    }
    private void setResultHandler(AbstractResultProcessor resultHandler) {
        this.resultHandler = resultHandler;
    }
    private void setHttp(CatHttp http) {
        this.http = http;
    }
    private void setJsonResolver(CatJsonResolver jsonResolver) {
        this.jsonResolver = jsonResolver;
    }

    public DefaultConfiguration getConfiguration(){
        return configuration;
    }

}
