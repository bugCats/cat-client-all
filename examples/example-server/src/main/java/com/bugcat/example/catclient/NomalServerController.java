package com.bugcat.example.catclient;


import com.alibaba.fastjson.JSONObject;
import com.bugcat.example.dto.Demo;
import com.bugcat.example.dto.DemoEntity;
import com.bugcat.example.tools.PageInfo;
import com.bugcat.example.tools.ResponseEntity;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


/**
 * 供 example-server 模块调用
 * */
@Api(tags = "服务端API - 一般场景")
@RestController
public class NomalServerController {

    
    @PostMapping(value = "/cat/demo1")
    public ResponseEntity<Demo> demo11(@Valid @RequestBody Demo req){
        System.out.println("demo1 >>> req: " + JSONObject.toJSONString(req));
        Demo resp = creart();
        return ResponseEntity.ok(resp);
    }


    @RequestMapping(value = "/cat/demo2", method = RequestMethod.POST)
    public ResponseEntity<Demo> demo12(@ModelAttribute DemoEntity req){
        System.out.println("demo2 >>> req: " + JSONObject.toJSONString(req));
        Demo resp = creart();
        return ResponseEntity.ok(resp);
    }
    
    
    
    @GetMapping("/cat/demo3")
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

    
    @GetMapping("/cat/demo4")
    public ResponseEntity<Demo> demo14(String name, String mark){

        System.out.println("demo4 >>> req: name=" + name + " mark=" + mark);

        Demo resp = creart();
        resp.setName(name);
        resp.setMark(mark);
        return ResponseEntity.ok(resp);
    }


    @GetMapping("/cat/demo5/{userId}")
    public Demo demo15(@PathVariable("userId") Long userId){
        System.out.println("demo5 >>> req: userId=" + userId);
        Demo resp = creart();
        resp.setId(userId); //这个会提示异常
        return resp;
    }
    


    @GetMapping("/cat/demo6/{userId}")
    public ResponseEntity<Void> demo16(@PathVariable("userId") Long userId, String name){
        System.out.println("demo6 >>> req: userId=" + userId + " name=" + name);
        return ResponseEntity.fail("自定义异常code", "自定义异常说明");
    }


    

    @GetMapping("/cat/demo7")
    public ResponseEntity<Demo> demo17(String name){
        Demo resp = creart();
        resp.setName(name);
        resp.setMark("正常拆包转器类");
        return ResponseEntity.ok(resp);
    }
    
    

    // 模拟服务器发生异常，测试异常回调
    @PostMapping(value = "/cat/demo21")
    public ResponseEntity<Demo> demo21(@RequestBody Demo req){
        System.out.println("demo1 >>> req: " + JSONObject.toJSONString(req));
        if( 1==1 ){
            throw new RuntimeException("发生了未知异常");
        }
        Demo demo = creart();
        return ResponseEntity.ok(demo);
    }
    
    
    // 实际使用post访问，测试异常回调
    @GetMapping(value = "/cat/demo22")
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
