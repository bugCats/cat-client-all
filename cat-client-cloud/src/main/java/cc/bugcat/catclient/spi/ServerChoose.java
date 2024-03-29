package cc.bugcat.catclient.spi;

import cc.bugcat.catclient.cloud.CatInstanceResolver;


/**
 * 负载均衡、注册中心适配器。
 * @author bugcat
 * */
public interface ServerChoose {



    /**
     * 根据serviceName获取实例地址。
     * @return "http://ip:port/ctx" or "http://www.bugcat.cc" or "https://bugcat.cc/ctx"
     * */
    String hostAddr(CatInstanceResolver instanceResolver);



    /**
     * http重连时执行。
     *
     * 如果启用http重连，第一次访问时，出现了http异常，后续再次重连的时候，负载均衡器可能又会把失效的实例返回。
     * 此时，如果有多个实例情况下，应该优先选取其他健康实例。
     *
     * @return "http://ip:port/ctx" or "http://www.bugcat.cc" or "https://bugcat.cc/ctx"
     * */
    String retryHostAddr(CatInstanceResolver instanceResolver);


}
