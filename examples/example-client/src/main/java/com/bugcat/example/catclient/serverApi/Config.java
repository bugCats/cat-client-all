package com.bugcat.example.catclient.serverApi;

import com.bugcat.catclient.annotation.CatClient;
import com.bugcat.catclient.handler.CatClients;
import com.bugcat.example.api.FaceDemoService;
import com.bugcat.example.api.UserService;

public interface Config extends CatClients {
    
    @CatClient(host = "${core-server.remoteApi}", connect = 3000, socket = 3000)
    UserService userService();


    @CatClient(host = "${core-server.remoteApi}", connect = -1, socket = -1)
    FaceDemoService faceDemoService();
    
}
