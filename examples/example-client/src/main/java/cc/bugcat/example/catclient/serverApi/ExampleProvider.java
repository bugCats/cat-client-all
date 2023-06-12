package cc.bugcat.example.catclient.serverApi;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.spi.CatClientProvider;
import cc.bugcat.example.api.UserService;
import cc.bugcat.example.api.FaceDemoService;

/**
 * 使用{@link CatClientProvider}子类方法定义客户端，避免@CatClient放到interface类上。
 * 可以保证interface类可以在客户端、服务端实例中均可引用。
 * */
public interface ExampleProvider extends CatClientProvider {

    @CatClient(host = "${core-server.remoteApi}", connect = 3000, socket = 3000)
    UserService userService();


    @CatClient(host = "${core-server.remoteApi}", connect = -1, socket = -1)
    FaceDemoService faceDemoService();

}
