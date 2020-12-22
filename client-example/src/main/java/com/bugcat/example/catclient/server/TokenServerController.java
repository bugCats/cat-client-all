package com.bugcat.example.catclient.server;


import com.alibaba.fastjson.JSONObject;
import com.bugcat.example.dto.Demo;
import com.bugcat.example.dto.ResponseEntity;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.TreeMap;


/**
 * 模拟远程服务端API接口
 * 使用单元测试类
 * */
@Api(tags = "服务端API - token")
@RestController
public class TokenServerController {


    /**
     * 模拟获取token
     * */
    @PostMapping(value = "/server/getToken")
    public ResponseEntity<String> getToken(@RequestParam("username") String username, @RequestParam("pwd") String pwd){
        System.out.println("username=" + username + "; pwd=" + pwd);
        return ResponseEntity.ok("[token=0123465]");
    }



    /**
     * 模拟获取token
     * */
    @PostMapping(value = "/server/token")
    public ResponseEntity<String> token(@RequestHeader("token") String token, @RequestBody Demo data){
        System.out.println(token);
        return ResponseEntity.ok(token);
    }
    
    
}
