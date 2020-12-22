package com.bugcat.example.catserver.server;


import com.alibaba.fastjson.JSONObject;
import com.bugcat.catserver.annotation.CatServer;
import com.bugcat.example.catserver.DemoService;
import com.bugcat.example.dto.Demo;
import com.bugcat.example.dto.DemoEntity;
import com.bugcat.example.dto.PageInfo;
import com.bugcat.example.dto.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;


/**
 * 加上注解@CatServer，该类等同于一个RestController
 * 但是 url、入参、请求方式，全部在interface中定义
 * 可以同时实现多个interface，但是不建议！
 * 如果多个interface中包含相同的url，会启动失败！当然，就算是一般情况有相同的url也是启动失败
 *
 * @author bugcat
 * */
@CatServer
public class ApiRemoteImpl implements ApiRemote1, ApiRemote2 {

    
    @Autowired
    private DemoService demoService;
    
    

    public ResponseEntity<Demo> demo1(Demo req) {
        System.out.println("demo11 >>> req: " + JSONObject.toJSONString(req));
        Demo resp = demoService.creart();
        return ResponseEntity.ok(resp);
    }

    public String demo2(DemoEntity req) {
        System.out.println("demo12 >>> req: " + JSONObject.toJSONString(req));
        Demo resp = demoService.creart();
        return "ok";
    }

    
    public ResponseEntity<PageInfo<Demo>> demo3(Demo req) {
        System.out.println("demo13 >>> req: " + JSONObject.toJSONString(req));

        Demo resp = demoService.creart();
        resp.setId(req.getId());
        List<Demo> list = new ArrayList<>();
        list.add(resp);

        PageInfo<Demo> info = new PageInfo(1, 10, 1);
        info.setList(list);

        return ResponseEntity.ok(info);
    }

    public ResponseEntity<Demo> demo4(String name, String mark) {

        System.out.println("demo14 >>> req: name=" + name + " mark=" + mark);

        Demo resp = demoService.creart();
        resp.setName(name);
        resp.setMark(mark);
        return ResponseEntity.ok(resp);
    }

    public Demo demo5(Long uid) {
        System.out.println("demo15 >>> req: userId=" + uid);
        Demo resp = demoService.creart();
        resp.setId(uid);
        return resp;
    }

    public Void demo6(Long uid, String name) {
        System.out.println("demo16 >>> req: userId=" + uid + " name=" + name);
        return null;
    }
    
    

}
