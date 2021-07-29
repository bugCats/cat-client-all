package cc.bugcat.example.catserver.serverApi;


import cc.bugcat.example.api.UserService;
import cc.bugcat.example.api.vi.UserPageVi;
import cc.bugcat.example.api.vi.UserSaveVi;
import cc.bugcat.example.api.vo.UserInfo;
import com.alibaba.fastjson.JSONObject;
import cc.bugcat.catserver.annotation.CatServer;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;


/**
 * 
 * 结合客户端完整示例
 * 
 * 通过[cc.bugcat.example.catclient.serverApi.ServerApiController]这个类发起调用
 * 
 * 
 * 
 * 
 * 通过示例可以发现:
 *  
 *  客户端自动注入UserService，可以像是用普通Service类一样，直接执行UserService.userPage等方法
 *  
 *  服务端实现UserService，可以像普通Service一样，实现抽象方法，然后就可以被客户端调用
 *  
 * 
 * @CatServer 也可以当作普通的Controller，在swagger上调用
 * 
 * */
@CatServer(handers = UserInterceptor.class)
public class UserServiceImpl implements UserService{


    @Override
    public ResponseEntity<PageInfo<UserInfo>> userPage(UserPageVi vi) {
        System.out.println("userPage >>> " + JSONObject.toJSONString(vi));

        UserInfo info = new UserInfo();
        info.setUid("666");
        info.setName("bugcat");
        info.setRemark("这是调用服务端userPage接口返回");

        PageInfo<UserInfo> page = new PageInfo<>(vi.getPageNum(), vi.getCount(), 1);
        page.setList(Arrays.asList(info));
        
        return ResponseEntity.ok(page);
    }

    @Override
    public UserInfo userInfo(String uid) {
        System.out.println("userInfo >>> " + uid);

        UserInfo info = new UserInfo();
        info.setUid("666");
        info.setName("bugcat");
        info.setRemark("这是调用服务端userInfo接口返回");

        return info;
    }

    @Override
    public ResponseEntity<Void> userSave(@RequestParam("name") UserSaveVi vi) {
        System.out.println("userSave >>> " + JSONObject.toJSONString(vi));
        return ResponseEntity.ok(null);
    }

    @Override
    public void status(String userId, String status) {
        System.out.println("userSave >>> userId=" + userId + ", status=" + status);
//        return null;
    }

    @Override
    public ResponseEntity<Void> method(String body) {
        System.out.println(body);
        return ResponseEntity.ok(null);
    }

    @Override
    public UserInfo delete(String userId) {
        System.out.println("模拟异常");
        if( true ){
            throw new RuntimeException("异常");
        }
        return null;
    }
}
