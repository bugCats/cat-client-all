package com.bugcat.catclient.handler;


import com.bugcat.catclient.annotation.CatClient;

/**
 * 使用方法标记{@link CatClient}，实现批量声明
 * 
 * 方法上的{@link CatClient}，优先度高于interface注解
 * 
 * */
public interface CatClients {
    
    
    /**
     * 
     *  public interface RemoteApi extends CatClients {
     *  
     *      @CatClient(host = "${userService.remoteApi}", connect = 3000, socket = 3000)
     *      UserService userService();
     *
     *      @CatClient(host = "${orderService.remoteApi}")
     *      OrderService orderService();
     *      
     *  }
     *
     * 
     * 
     * 
     * */
    
    
}
