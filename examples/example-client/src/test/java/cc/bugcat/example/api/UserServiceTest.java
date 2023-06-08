package cc.bugcat.example.api;

import cc.bugcat.catclient.handler.CatClientDepend;
import cc.bugcat.catclient.utils.CatClientBuilders;
import cc.bugcat.example.api.vi.UserPageVi;
import cc.bugcat.example.api.vi.UserSaveVi;
import cc.bugcat.example.api.vo.UserInfo;
import cc.bugcat.example.catclient.serverApi.Config;
import cc.bugcat.example.tools.PageInfo;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.alibaba.fastjson.JSONObject;
import cc.bugcat.catclient.utils.CatClientUtil;
import cc.bugcat.example.tools.ResponseEntity;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class UserServiceTest{

    private static UserService userService;

    static {
        ((Logger) LoggerFactory.getLogger("ROOT")).setLevel(Level.ERROR);

        Properties prop = new Properties();
        prop.put("core-server.remoteApi", "http://127.0.0.1:8012");
        
        CatClientDepend clientDepend = CatClientDepend.builder().environment(prop).build();
        userService =  CatClientBuilders.builder(Config.class, UserService.class)
                .clientDepend(clientDepend)
                .build();
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
