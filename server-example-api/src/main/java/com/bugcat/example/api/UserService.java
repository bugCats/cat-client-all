package com.bugcat.example.api;


import com.bugcat.catclient.annotation.CatClient;
import com.bugcat.catclient.annotation.CatMethod;
import com.bugcat.example.api.vi.UserPageVi;
import com.bugcat.example.api.vi.UserSaveVi;
import com.bugcat.example.tools.PageInfo;
import com.bugcat.example.tools.ResponseEntity;
import com.bugcat.example.api.vo.UserInfo;
import com.bugcat.example.tools.ResponseEntityWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;


/**
 * 
 * 配合@FeignClient，服务端直接通过 @FeignClient标记的interface，生成Controller
 * 
 * 示例中没有引入Feign包，其实有没有Feign不影响结果
 * 
 * 
 * 方法上@CatMethod注解，可以换成标准的@RequestMapping、@GetMapping等
 * 
 * 
 * */


//@FeignClient

@Api(tags = "用户操作api")
@CatClient(host = "${core-server.remoteApi}", wrapper = ResponseEntityWrapper.class, connect = 3000, socket = 3000)
public interface UserService {
    
    @ApiOperation("分页查询用户")
    @CatMethod(value = "/user/userPage")
    ResponseEntity<PageInfo<UserInfo>> userPage(@ModelAttribute UserPageVi vi);



    @ApiOperation("根据用户id查询用户信息")
    @CatMethod(value = "/user/get/{uid}", method = RequestMethod.GET)
    UserInfo userInfo(@PathVariable("uid") @RequestBody @RequestParam("status") String uid);



    @ApiOperation("编辑用户")
    @CatMethod(value = "/user/save", method = RequestMethod.POST)
    ResponseEntity<Void> userSave(@RequestBody UserSaveVi vi) throws Exception;



    @ApiOperation("设置用户状态")
    @CatMethod(value = "/user/status", method = RequestMethod.GET)
    Void status(@RequestParam("uid") String userId, @RequestParam("status") String status);

}
