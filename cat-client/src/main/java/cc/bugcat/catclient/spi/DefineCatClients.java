package cc.bugcat.catclient.spi;


import cc.bugcat.catclient.annotation.CatClient;

/**
 *
 * 使用方法标记{@link CatClient}，实现批量注册客户端类。
 *
 * 
 * <pre>
 *  public interface RemoteApis extends DefineCatClients {
 *
 *      //注册UserService为客户端
 *      @CatClient(host = "${userService.remoteApi}", connect = 3000, socket = 3000)
 *      UserService userService();
 *
 *      //注册OrderService为客户端
 *      @CatClient(host = "${orderService.remoteApi}")
 *      OrderService orderService();
 *
 *  }
 *  </pre>
 *
 *
 *  此种方式注册客户端，方法上的{@link CatClient}，优先度高于interface上的注解。
 *
 *  {@code @CatClient}注解放在方法上，使用这种方式注册，可以避免在interface类上修改，并且能提高interface复用性，也可以减少耦合。
 *  
 *
 *  在精简模式下，客户端与服务端，共用同一个interface类。
 *  
 *  使用这种方式注册CatClient客户端类，避免{@code @CatClient}污染interface，而造成不必要的问题。
 *
 *
 * @author bugcat
 * */
public interface DefineCatClients {

    

/**

    @EnableCatClient(classes = {RemoteApis.class})
    @SpringBootApplication
    public class Application {
        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }
    }
    
    
    public interface RemoteApis extends DefineCatClients {
    
        @CatClient(host = "${userService.remoteApi}", connect = 3000, socket = 3000)
        UserService userService();
        
        @CatClient(host = "${orderService.remoteApi}")
        OrderService orderService();
    
    }
     
 * */

    
}
