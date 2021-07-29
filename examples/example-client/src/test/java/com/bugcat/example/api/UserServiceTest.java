package com.bugcat.example.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.alibaba.fastjson.JSONObject;
import com.bugcat.catclient.utils.CatClientUtil;
import com.bugcat.example.api.vi.UserPageVi;
import com.bugcat.example.api.vi.UserSaveVi;
import com.bugcat.example.api.vo.UserInfo;
import com.bugcat.example.catclient.serverApi.Config;
import com.bugcat.example.tools.PageInfo;
import com.bugcat.example.tools.ResponseEntity;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class UserServiceTest{

    private static UserService userService;

    static {
        ((Logger) LoggerFactory.getLogger("ROOT")).setLevel(Level.ERROR);

        Properties prop = new Properties();
        prop.put("core-server.remoteApi", "http://127.0.0.1:8012");
        userService = CatClientUtil.proxy(Config.class, UserService.class, prop);
    }
    

    @Test
    public void userPage(){
        UserPageVi vi = new UserPageVi();
        vi.setName("bugcat");
        ResponseEntity<PageInfo<UserInfo>> page = userService.userPage(vi);
        System.out.println(JSONObject.toJSONString(page));
    }


    @Test
    public void userInfo(){
        UserInfo userInfo = userService.userInfo("6666");
        System.out.println(JSONObject.toJSONString(userInfo));
    }


    @Test
    public void userSave(){
        try {
            UserSaveVi vi = new UserSaveVi();
            //            vi.setName("bugcat");
            vi.setEmail("972245132@qq.com");
            ResponseEntity<Void> status = userService.userSave(vi);
            System.out.println(JSONObject.toJSONString(status));

        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }


    @Test
    public void status(){
        userService.status("6666", "1");
    }


    @Test
    public void delete(){
        UserInfo delete = userService.delete("3622");
        System.out.println(JSONObject.toJSONString(delete));
    }



    @Test
    public void method(){
        ResponseEntity<Void> resp = userService.method("hello world");
        System.out.println(JSONObject.toJSONString(resp));
    }



}
