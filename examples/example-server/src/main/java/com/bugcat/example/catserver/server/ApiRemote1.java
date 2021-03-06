package com.bugcat.example.catserver.server;

import com.bugcat.example.dto.Demo;
import com.bugcat.example.dto.DemoEntity;
import com.bugcat.example.tools.PageInfo;
import com.bugcat.example.tools.ResponseEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


/**
 * 定义Controller
 * 可以直接在swagger测试调用
 * @author: bugcat
 * */
@Api(tags = "服务端API - 一般场景1")
public interface ApiRemote1 {
    

    @ApiOperation(value = "服务端示例1")
    @PostMapping("/server/demo1")
    ResponseEntity<Demo> demo1(@RequestBody Demo req);



    @ApiOperation(value = "服务端示例2")
    @RequestMapping(value = "/server/demo2", method = RequestMethod.POST)
    String demo2(@ModelAttribute("req") DemoEntity req);



    @ApiOperation(value = "服务端示例3")
    @GetMapping("/server/demo3")
    ResponseEntity<PageInfo<Demo>> demo3(@ModelAttribute Demo req);
   
    

    
}
