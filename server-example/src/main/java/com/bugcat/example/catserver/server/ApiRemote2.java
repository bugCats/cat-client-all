package com.bugcat.example.catserver.server;

import com.bugcat.example.dto.Demo;
import com.bugcat.example.dto.DemoEntity;
import com.bugcat.example.dto.PageInfo;
import com.bugcat.example.dto.ResponseEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;


/**
 *
 * @author: bugcat
 * */
@Api(tags = "服务端API - 一般场景2")
public interface ApiRemote2 {


    @ApiOperation(value = "服务端示例4")
    @RequestMapping(value = "/server/demo4", method = RequestMethod.GET)
    ResponseEntity<Demo> demo4(@RequestParam("name") String name, @RequestParam("mark") String mark);



    @ApiOperation(value = "服务端示例5")
    @GetMapping("/server/demo5/{userId}")
    Demo demo5(@PathVariable("userId") Long uid);



    @ApiOperation(value = "服务端示例6")
    @GetMapping(value = "/server/demo6/{userId}")
    Void demo6(@PathVariable("userId") Long uid, @RequestParam("name") String uname);

    
}
