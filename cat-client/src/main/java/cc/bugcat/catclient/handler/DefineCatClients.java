package cc.bugcat.catclient.handler;


import cc.bugcat.catclient.annotation.CatClient;

/**
 * 使用方法标记{@link CatClient}，实现批量声明。
 *
 * 方法上的{@link CatClient}，优先度高于interface上的注解。
 *
 * 避免直接修改interface类，减少耦合。
 *
 *
 * <pre>
 *  public interface RemoteApi extends DefineCatClients {
 *
 *      @CatClient(host = "${userService.remoteApi}", connect = 3000, socket = 3000)
 *      UserService userService(); //定义UserService为客户端。方法上的@CatClient注解，优先度高于interface类上的
 *
 *      @CatClient(host = "${orderService.remoteApi}")
 *      OrderService orderService();  //定义OrderService为客户端。方法上的@CatClient注解，优先度高于interface类上的
 *
 *  }
 *  </pre>
 *
 *
 * */
public interface DefineCatClients {



}
