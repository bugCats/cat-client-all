package cc.bugcat.example.api;

import cc.bugcat.catclient.annotation.CatMethod;
import cc.bugcat.catface.annotation.CatNote;
import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.example.api.vo.UserInfo;
import cc.bugcat.example.tools.ResponseEntityWrapper;
import cc.bugcat.example.api.vi.UserPageVi;
import cc.bugcat.example.api.vi.UserSaveVi;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Component;
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
 * CatClient 可以忽略，在客户端重新新建一个interface，继承UserService，在新interface上加@CatClient
 * 参见cc.bugcat.example.catclient.remote.ApiRemote4Ext
 *
 * 或者使用 CatClients 方式：
 * <pre>
 * public interface Config extends CatClients {
 *
 *      @CatClient(host = "${core-server.remoteApi}", getConnect = 3000, getSocket = 3000)
 *      UserService service();
 *
 * }
 * </pre>
 *
 * CatResponesWrapper 统一为响应添加包装器类。如果响应本身就是包装器类，则忽略
 * 可以仔细查看swagger文档、和接口响应日志
 * 会发现响应外层自动加了一层ResponseEntity
 *
 *
 * */



@Component // 可以省略
@Api(tags = "用户操作api")
@CatResponesWrapper(ResponseEntityWrapper.class)
public interface UserService {



    @ApiOperation("分页查询用户")
    @CatMethod(value = "/user/userPage")
    ResponseEntity<PageInfo<UserInfo>> userPage(@ModelAttribute("vi") UserPageVi vi);


    @ApiOperation("根据用户id查询用户信息")
    @CatMethod(value = "/user/get/{uid}", method = RequestMethod.GET, notes = @CatNote("user"))
    UserInfo userInfo(@PathVariable("uid") String uid);


    @ApiOperation("编辑用户")
    @CatMethod(value = "/user/save", method = RequestMethod.POST, notes = @CatNote(key = "name", value = "#{vi.name}"))
    ResponseEntity<Void> userSave(@RequestBody @CatNote("vi") UserSaveVi vi) throws Exception;


    @ApiOperation("设置用户状态")
    @CatMethod(value = "/user/status", method = RequestMethod.GET)
    void status(@RequestParam("uid") String userId, @RequestParam("status") String status);


    @ApiOperation("删除用户状态")
    @CatMethod(value = "/user/delete/{uid}", method = RequestMethod.GET)
    UserInfo delete(@PathVariable("uid") String userId);




    @ApiOperation("删除用户状态")
    @CatMethod(value = "/user/method", method = RequestMethod.POST)
    ResponseEntity<Void> method(@RequestBody String body);



}
