package com.bugcat.example.catclient.remote;

import com.alibaba.fastjson.JSONObject;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.example.dto.Demo;
import com.bugcat.example.dto.DemoEntity;
import com.bugcat.example.tools.PageInfo;
import com.bugcat.example.tools.ResponseEntity;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 客户端
 * 
 * 也可以使用 ApiRemote1Test、ApiRemote2Test、ApiRemote3Test 等单元测试类
 * 
 * */
@Api(tags = "客户端 - 一般场景")
@RestController
public class NomalController {

    @Autowired
    private ApiRemote1 catRemoteApi;   //呆毛1
    
    
    @GetMapping("/cat1")
    public String cat1(){
        Demo demo = creart();
        demo.setId(null);
        ResponseEntity<Demo> resp = catRemoteApi.demo1(demo);
        return JSONObject.toJSONString(resp);
    }

    
    @GetMapping("/cat2")
    public String cat2(){
        Demo demo = creart();
        SendProcessor sendHandler = new SendProcessor();
        String resp = catRemoteApi.demo2(sendHandler, new DemoEntity(demo));
        StringBuilder sbr = new StringBuilder();
        sbr.append("resp=").append(JSONObject.toJSONString(resp)).append("<br/>");
        sbr.append("req=").append(sendHandler.getReqStr()).append("<br/>"); //此次http调用的入参、响应都砸sendHandler中
        return sbr.toString();
    }
    
    
    @GetMapping("/cat3")
    public String cat3(){

        Demo demo = creart();
        StringBuilder sbr = new StringBuilder();
        
        SendProcessor sendHandler = new SendProcessor();

        ResponseEntity<PageInfo<Demo>> resp = catRemoteApi.demo3(demo, sendHandler);
        sbr.append("第一次=").append(JSONObject.toJSONString(resp)).append("<br/>");
        
        demo.setId(3L);
        resp = catRemoteApi.demo3(demo, sendHandler);
        sbr.append("第二次=").append(JSONObject.toJSONString(resp)).append("<br/>");
        
        return sbr.toString();
    }
    
    @GetMapping("/cat4")
    public ResponseEntity<Demo> cat4(){
        ResponseEntity<Demo> resp = catRemoteApi.demo4("bug猫", "bug猫");
        return resp;
    }

    @GetMapping("/cat5")
    public Demo cat5(){
        Demo resp = catRemoteApi.demo5(System.currentTimeMillis());
        return resp;
    }

    
    @GetMapping("/cat6")
    public ResponseEntity<Void> cat6(){
        SendProcessor sendHandler = new SendProcessor();
        catRemoteApi.demo6(System.currentTimeMillis(), sendHandler, "bug猫");
        return ResponseEntity.ok(null);
    }

    @GetMapping("/cat9")
    public ResponseEntity<String> cat9(){
        ResponseEntity<String> resp = catRemoteApi.demo9();
        return resp;
    }


    private Demo creart(){
        Demo demo = new Demo();
        demo.setId(System.currentTimeMillis());
        demo.setName("bug猫");
        demo.setMark("调用");
        return demo;
    }
}
