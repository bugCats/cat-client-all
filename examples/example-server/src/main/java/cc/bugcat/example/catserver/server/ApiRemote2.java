package cc.bugcat.example.catserver.server;

import cc.bugcat.example.dto.Demo;
import cc.bugcat.example.tools.ResponseEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;


/**
 * 定义Controller
 * 可以直接在swagger测试调用
 * @author: bugcat
 * */
@Api(tags = "CatServer - 继承子类")
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
