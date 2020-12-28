package com.bugcat.example.catclient.serverApi;


import com.alibaba.fastjson.JSONObject;
import com.bugcat.example.api.UserService;
import com.bugcat.example.api.vi.UserPageVi;
import com.bugcat.example.api.vi.UserSaveVi;
import com.bugcat.example.tools.PageInfo;
import com.bugcat.example.tools.ResponseEntity;
import com.bugcat.example.api.vo.UserInfo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 
 * 联合服务端完整示例
 * 必须启动 server-example
 * 
 * 
 * 此处模拟客户端环境，通过注入UserService，调用方法，发起http请求
 * 
 * 服务类在：com.bugcat.example.catserver.serverApi.UserServiceImpl
 * 
 * 
 * */
@Api(tags = "客户端 - cat-server")
@RestController
public class ServerApiController {

    @Autowired
    private UserService userService;


    @GetMapping("/test/userPage")
    public void userPage(){
        UserPageVi vi = new UserPageVi();
        vi.setName("bugcat");
        ResponseEntity<PageInfo<UserInfo>> page = userService.userPage(vi);
        System.out.println(JSONObject.toJSONString(page));
    }


    @GetMapping("/test/userInfo")
    public void userInfo(){
        UserInfo userInfo = userService.userInfo("6666");
        System.out.println(JSONObject.toJSONString(userInfo));
    }

    @GetMapping("/test/userSave")
    public void userSave(){
        try {
            
            UserSaveVi vi = new UserSaveVi();
            vi.setName("bugcat");
            vi.setEmail("972245132@qq.com");
            ResponseEntity<Void> status = userService.userSave(vi);
            System.out.println(JSONObject.toJSONString(status));
            
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
    

    @GetMapping("/test/status")
    public void status(){
        Void status = userService.status("6666", "1");
        System.out.println(JSONObject.toJSONString(status));
    }
    
}
