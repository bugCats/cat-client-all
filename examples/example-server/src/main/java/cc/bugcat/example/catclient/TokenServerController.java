package cc.bugcat.example.catclient;


import cc.bugcat.example.dto.Demo;
import cc.bugcat.example.tools.ResponseEntity;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * 供 example-server 模块调用
 * */
@Api(tags = "服务端API - token")
@RestController
public class TokenServerController {


    /**
     * 模拟获取token
     * */
    @PostMapping(value = "/cat/getToken")
    public ResponseEntity<String> getToken(@RequestParam("username") String username, @RequestParam("pwd") String pwd){
        System.out.println("username=" + username + "; pwd=" + pwd);
        return ResponseEntity.ok("[token=0123465]");
    }



    /**
     * 模拟获取token
     * */
    @PostMapping(value = "/cat/token")
    public ResponseEntity<String> token(@RequestHeader("token") String token, @RequestBody Demo data){
        System.out.println("data=" + JSONObject.toJSONString(data));
        System.out.println("token=" + token);
        return ResponseEntity.ok(token);
    }
    
    
}
