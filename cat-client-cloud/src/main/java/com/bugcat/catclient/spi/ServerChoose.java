package com.bugcat.catclient.spi;

public interface ServerChoose {
    
    
    /**
     * 根据serviceName获取实例地址
     * 
     * @return http://xxxxxx/xxx
     * */
    String hostAddr(String serviceName);


    
    /**
     * 根据serviceName获取实例地址，并且排除hostAddr这个地址
     * 
     * 如果启用http重连，第一次访问时，出现了http异常，后续再次重连的时候，负载均衡器可能又会把失效的实例返回
     * 
     * 此时，如果有多个实例情况下，应该优先选取其他健康实例。否则返回hostAddr
     *
     * @return http://xxxxxx/xxx
     * */
    String hostAddr(String serviceName, String hostAddr);
    
    
}
