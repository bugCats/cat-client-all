package cc.bugcat.example.catserver.server;


import cc.bugcat.catserver.annotation.CatServer;
import cc.bugcat.example.catserver.DemoService;
import cc.bugcat.example.dto.Demo;
import cc.bugcat.example.dto.DemoEntity;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;


/**
 * 加上注解//@CatServer，该类等同于一个RestController
 * 但是 url、入参、请求方式，全部在interface中定义
 *
 * 可以同时实现多个interface，但是<b>不建议！</b>
 * 如果多个interface中包含相同的方法、或者url，会启动失败！当然，就算是一般情况有相同的url也是启动失败
 *
 * @author bugcat
 * */

@CatServer // 全局拦截器+拦截器组
public class ApiRemoteImpl implements ApiRemote1, ApiRemote2 {


    @Autowired
    private DemoService demoService;


    //被子类重写
    public ResponseEntity<Demo> demo1(Demo req) {
        System.out.println("demo11 >>> req: " + JSONObject.toJSONString(req));
        Demo resp = demoService.creart();
        return ResponseEntity.ok(resp);
    }

    //被子类重写
    public String demo2(DemoEntity req) {
        System.out.println("demo12 >>> req: " + JSONObject.toJSONString(req));
        Demo resp = demoService.creart();
        return "ok";
    }


    //被子类重写
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
