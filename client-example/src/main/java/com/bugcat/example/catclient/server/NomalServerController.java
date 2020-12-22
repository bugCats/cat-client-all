package com.bugcat.example.catclient.server;


import com.alibaba.fastjson.JSONObject;
import com.bugcat.example.dto.Demo;
import com.bugcat.example.dto.DemoEntity;
import com.bugcat.example.dto.PageInfo;
import com.bugcat.example.dto.ResponseEntity;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * 模拟远程服务端API接口
 * 请通过 NomalController 发起调用，或者使用单元测试类
 * */
@Api(tags = "服务端API - 一般场景")
@RestController
public class NomalServerController {

    
    @PostMapping(value = "/server/demo1")
    public ResponseEntity<Demo> demo11(@RequestBody Demo req){
        System.out.println("demo1 >>> req: " + JSONObject.toJSONString(req));
        Demo resp = creart();
        return ResponseEntity.ok(resp);
    }


    @RequestMapping(value = "/server/demo2", method = RequestMethod.POST)
    public ResponseEntity<Demo> demo12(@ModelAttribute DemoEntity req){
        System.out.println("demo2 >>> req: " + JSONObject.toJSONString(req));
        Demo resp = creart();
        return ResponseEntity.ok(resp);
    }
    
    
    
    @GetMapping("/server/demo3")
    public ResponseEntity<PageInfo<Demo>> demo13(@ModelAttribute Demo req){

        System.out.println("demo3 >>> req: " + JSONObject.toJSONString(req));

        Demo resp = creart();
        resp.setId(req.getId());
        List<Demo> list = new ArrayList<>();
        list.add(resp);
        
        PageInfo<Demo> info = new PageInfo(1, 10, 1);
        info.setList(list);
        
        return ResponseEntity.ok(info);
    }

    
    @GetMapping("/server/demo4")
    public ResponseEntity<Demo> demo14(String name, String mark){

        System.out.println("demo4 >>> req: name=" + name + " mark=" + mark);

        Demo resp = creart();
        resp.setName(name);
        resp.setMark(mark);
        return ResponseEntity.ok(resp);
    }


    @GetMapping("/server/demo5/{userId}")
    public Demo demo15(@PathVariable("userId") Long userId){
        System.out.println("demo5 >>> req: userId=" + userId);
        Demo resp = creart();
        resp.setId(userId);
        return resp;
    }
    


    @GetMapping("/server/demo6/{userId}")
    public ResponseEntity<Void> demo16(@PathVariable("userId") Long userId, String name){
        System.out.println("demo6 >>> req: userId=" + userId + " name=" + name);
        return ResponseEntity.fail("自定义异常code", "自定义异常说明");
    }






    // 模拟服务器发生异常，测试异常回调
    @PostMapping(value = "/server/demo21")
    public ResponseEntity<Demo> demo21(@RequestBody Demo req){
        System.out.println("demo1 >>> req: " + JSONObject.toJSONString(req));
        if( 1==1 ){
            throw new RuntimeException("发生了未知异常");
        }
        Demo demo = creart();
        return ResponseEntity.ok(demo);
    }
    // 实际使用post访问，测试异常回调
    @GetMapping(value = "/server/demo22")
    String demo2(Demo req) {
        return "";
    }

    
    

    private Demo creart(){
        Demo demo = new Demo();
        demo.setId(2L);
        demo.setName("bugcat");
        demo.setMark("服务端");
        return demo;
    }
}
