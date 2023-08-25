package cc.bugcat.catclient.spi;

import cc.bugcat.catclient.handler.CatClientMockProvideBuilder;

import java.util.Collections;
import java.util.Set;


/**
 * 
 * 客户端mock功能；
 * 当对应的服务端没有启动，客户端又需要启用时，可以开启
 * 
 * */
public interface CatClientMockProvide {
    
    
    public static CatClientMockProvideBuilder builder(){
        return new CatClientMockProvideBuilder();
    }
    
    /**
     * 是否启用mock；
     * */
    default boolean enableMock(){
        return false;
    }
    
    
    /**
     * 需要mock的客户端interface的实现类、或对象；
     * */
    default Set<Class> mockClients(){
        return Collections.emptySet();
    }
    
    
}
