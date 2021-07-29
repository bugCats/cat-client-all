package cc.bugcat.catclient.handler;


import cc.bugcat.catclient.annotation.CatClient;

/**
 * 使用方法标记{@link CatClient}，实现批量声明
 * 
 * 方法上的{@link CatClient}，优先度高于interface上的注解
 * 
 * <pre>
 *  public interface RemoteApi extends CatClients {
 *
 *      @CatClient(host = "${userService.remoteApi}", connect = 3000, socket = 3000)
 *      UserService userService();
 *
 *      @CatClient(host = "${orderService.remoteApi}")
 *      OrderService orderService();
 *
 *  }
 *  </pre>
 *  
 * 避免直接修改interface类，减少耦合
 * 
 * 
 * */
public interface CatClients {
    
    
    
}
