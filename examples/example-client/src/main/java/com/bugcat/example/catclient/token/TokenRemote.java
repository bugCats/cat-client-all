package com.bugcat.example.catclient.token;

import com.bugcat.catclient.annotation.CatClient;
import com.bugcat.catclient.annotation.CatMethod;
import com.bugcat.catclient.annotation.CatNote;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.example.dto.Demo;
import com.bugcat.example.tools.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 
 * 需要token验证版
 * 单元测试类 @com.bugcat.example.catclient.token.TokenRemoteTest
 *
 * @author: bugcat
 * */
@CatClient(host = "${core-server.remoteApi}", factory = TokenFactory.class, connect = 3000, socket = 3000)
public interface TokenRemote {
    

    

    @CatMethod(value = "/cat/getToken", method = RequestMethod.POST, 
            notes = {@CatNote(key = "username", value = "${demo.username}"),
                    @CatNote(key = "pwd", value = "${demo.pwd}")})
    ResponseEntity<String> getToken(SendProcessor sender, @RequestParam("username") String username, @RequestParam("pwd") String pwd);




    @CatMethod(value = "/cat/token", method = RequestMethod.POST, notes = @CatNote("needToken"))
    ResponseEntity<String> token1(@RequestBody Demo demo);



    @CatMethod(value = "/cat/token", method = RequestMethod.POST)
    ResponseEntity<String> token2(@RequestBody Demo demo, @RequestHeader("token") String token);


    @CatMethod(value = "{url}", method = RequestMethod.POST)
    ResponseEntity<String> token3(@PathVariable("url") String url, @RequestHeader("token") String token, @RequestBody String req);

}
