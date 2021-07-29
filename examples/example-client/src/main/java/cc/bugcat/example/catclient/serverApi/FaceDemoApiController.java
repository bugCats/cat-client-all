package cc.bugcat.example.catclient.serverApi;


import cc.bugcat.example.api.vo.UserInfo;
import cc.bugcat.example.api.FaceDemoService;
import cc.bugcat.example.api.vi.UserPageVi;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * 联合服务端完整示例
 * 必须启动 server-example
 * 
 * 
 * 此处模拟客户端环境，通过注入FaceDemoService，调用方法，发起http请求
 * 或者cc.bugcat.example.api.FaceDemoServiceTest调用
 * 
 * 服务类在：cc.bugcat.example.catserver.serverApi.FaceDemoServiceImpl
 * 
 * 
 * */
@Api(tags = "客户端 - FaceDemo")
@RestController
public class FaceDemoApiController{

    @Autowired
    private FaceDemoService faceDemoService;

    @ApiOperation("入参0")
    @GetMapping("/face/param0")
    public UserInfo param0() {
        return faceDemoService.param0();
    }


    @ApiOperation("入参1")
    @GetMapping("/face/param1")
    public UserInfo param1() {
        return faceDemoService.param1("userId");
    }

    @ApiOperation("入参2")
    @GetMapping("/face/param2")
    public UserInfo param2() {
        return faceDemoService.param2("userId", 1);
    }

    @ApiOperation("入参3")
    @GetMapping("/face/param3")
    public UserInfo param3() {
        UserPageVi vi = new UserPageVi();
        vi.setUid("param3");
        vi.setName("入参3");
        return faceDemoService.param3(vi);
    }

    @ApiOperation("入参4")
    @GetMapping("/face/param4")
    public UserInfo param4() {
        UserPageVi vi = new UserPageVi();
        vi.setUid("param4");
        vi.setName("入参4");
        return faceDemoService.param4("userId", vi);
    }

    @ApiOperation("入参5")
    @GetMapping("/face/param5")
    public UserInfo param5() {
        UserPageVi vi = new UserPageVi();
        vi.setUid("param5");
        vi.setName("入参5");
        return faceDemoService.param5("userId", vi, 1);
    }

    @ApiOperation("入参6")
    @GetMapping("/face/param6")
    public UserInfo param6() {
        UserPageVi vi1 = new UserPageVi();
        vi1.setUid("param61");
        vi1.setName("入参61");
        UserPageVi vi2 = new UserPageVi();
        vi2.setUid("param62");
        vi2.setName("入参62");
        return faceDemoService.param6(vi1, vi2, 1);
    }

    @ApiOperation("入参7")
    @GetMapping("/face/param7")
    public UserInfo param7() {
        UserPageVi vi1 = new UserPageVi();
        vi1.setUid("param61");
        vi1.setName("入参61");
        UserPageVi vi2 = new UserPageVi();
        vi2.setUid("param62");
        vi2.setName("入参62");
        Map<String, Object> map = new HashMap<>();
        map.put("mapKey1", "value1");
        map.put("mapKey2", "value2");
        return faceDemoService.param7(vi1, vi2, 1, map);
    }



    @ApiOperation("入参8")
    @GetMapping("/face/param8")
    public UserInfo param8() {
        UserPageVi vi1 = new UserPageVi();
        vi1.setUid("param61");
        vi1.setName("入参61");
        UserPageVi vi2 = new UserPageVi();
        vi2.setUid("param62");
        vi2.setName("入参62");
        
        UserPageVi vi31 = new UserPageVi();
        vi31.setUid("param63");
        vi31.setName("入参63");
        PageInfo<UserPageVi> vi32 = new PageInfo<>(1, 10, 1);
        vi32.setList(Collections.singletonList(vi31));
        ResponseEntity<PageInfo<UserPageVi>> vi3 = ResponseEntity.ok(vi32);
        
        Map<String, Object> map = new HashMap<>();
        map.put("mapKey1", "value1");
        map.put("mapKey2", "value2");
        
        return faceDemoService.param8(map, vi1, vi2, 1, vi3);
    }


    @ApiOperation("入参9")
    @GetMapping("/face/param9")
    public UserInfo param9() {
        UserPageVi vi1 = new UserPageVi();
        vi1.setUid("param61");
//        vi1.setName("入参61");

        UserPageVi vi31 = new UserPageVi();
        vi31.setUid("param63");
        vi31.setName("入参63");
        PageInfo<UserPageVi> vi32 = new PageInfo<>(1, 10, 1);
        vi32.setList(Collections.singletonList(vi31));
        ResponseEntity<PageInfo<UserPageVi>> vi3 = ResponseEntity.ok(vi32);

        Map<String, Object> map = new HashMap<>();
        map.put("mapKey1", "value1");
        map.put("mapKey2", "value2");

        return faceDemoService.param9(map, vi1, new Date(), 1, null, vi3);
    }

}
