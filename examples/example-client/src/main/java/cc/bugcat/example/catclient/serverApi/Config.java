package cc.bugcat.example.catclient.serverApi;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.handler.CatClients;
import cc.bugcat.example.api.UserService;
import cc.bugcat.example.api.FaceDemoService;

public interface Config extends CatClients {
    
    @CatClient(host = "${core-server.remoteApi}", connect = 3000, socket = 3000)
    UserService userService();


    @CatClient(host = "${core-server.remoteApi}", connect = -1, socket = -1)
    FaceDemoService faceDemoService();
    
}
