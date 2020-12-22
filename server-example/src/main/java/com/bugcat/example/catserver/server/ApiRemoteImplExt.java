package com.bugcat.example.catserver.server;


import com.alibaba.fastjson.JSONObject;
import com.bugcat.catserver.annotation.CatServer;
import com.bugcat.example.catserver.DemoService;
import com.bugcat.example.dto.Demo;
import com.bugcat.example.dto.DemoEntity;
import com.bugcat.example.dto.PageInfo;
import com.bugcat.example.dto.ResponseEntity;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * 没错！支持继承！
 * 子类重写父类方法之后，就实现了api升级！
 * 
 * @author bugcat
 * 
 * */
@Api(tags = "服务端API - 继承类")
@CatServer
public class ApiRemoteImplExt extends ApiRemoteImpl {


    @Autowired
    private DemoService demoService;
    

    public ResponseEntity<Demo> demo1(Demo req) {
        System.out.println("demo21 >>> req: " + JSONObject.toJSONString(req));
        Demo resp = demoService.creart();
        return ResponseEntity.ok(resp);
    }

    public String demo2(DemoEntity req) {
        System.out.println("demo22 >>> req: " + JSONObject.toJSONString(req));
        Demo resp = demoService.creart();
        return "ok";
    }

    
    public ResponseEntity<PageInfo<Demo>> demo3(Demo req) {
        System.out.println("demo23 >>> req: " + JSONObject.toJSONString(req));

        Demo resp = demoService.creart();
        resp.setId(req.getId());
        List<Demo> list = new ArrayList<>();
        list.add(resp);

        PageInfo<Demo> info = new PageInfo(1, 10, 1);
        info.setList(list);

        return ResponseEntity.ok(info);
    }
    

    public Demo demo5(Long uid) {
        System.out.println("demo25 >>> req: userId=" + uid);
        Demo resp = demoService.creart();
        resp.setId(uid);
        return resp;
    }

    
    
}
