package cc.bugcat.example.api;

import cc.bugcat.catclient.handler.CatClientDepend;
import cc.bugcat.catclient.utils.CatClientBuilders;
import cc.bugcat.example.api.vi.UserPageVi;
import cc.bugcat.example.api.vo.UserInfo;
import cc.bugcat.example.catclient.serverApi.ExampleProvider;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import static org.assertj.core.api.Assertions.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class FaceDemoServiceTest{

    private static FaceDemoService faceDemoService;

    
    @BeforeClass
    public static void beforeClass(){
        ((Logger) LoggerFactory.getLogger("ROOT")).setLevel(Level.ERROR);

        Properties prop = new Properties();
        prop.put("core-server.remoteApi", "http://127.0.0.1:8012");

        CatClientDepend clientDepend = CatClientDepend.builder().environment(prop).build();
        faceDemoService = CatClientBuilders.builder(ExampleProvider.class, FaceDemoService.class)
                .clientDepend(clientDepend)
                .build();
    }


    @Test
    public void param0() {
        UserInfo userInfo = faceDemoService.param0();
        assertThat(userInfo).isNotNull().hasFieldOrPropertyWithValue("name", "param0");
    }

    @Test
    public void param1() {
        UserInfo userInfo = faceDemoService.param1("userId");
        assertThat(userInfo).isNotNull().hasFieldOrPropertyWithValue("name", "userId");
    }

    @Test
    public void param2() {
        UserInfo userInfo = faceDemoService.param2("userId", 1);
        assertThat(userInfo).isNotNull().hasFieldOrPropertyWithValue("name", "userId-1");
    }

    @Test
    public void param3() {
        UserPageVi vi = new UserPageVi();
        vi.setUid("param3");
        vi.setName("入参3");
        UserInfo userInfo = faceDemoService.param3(vi);
        assertThat(userInfo).isNotNull().hasFieldOrPropertyWithValue("name", "param3-入参3");
    }

    @Test
    public void param4() {
        UserPageVi vi = new UserPageVi();
        vi.setUid("param4");
        vi.setName("入参4");
        UserInfo userInfo = faceDemoService.param4("userId", vi);
        assertThat(userInfo).isNotNull().hasFieldOrPropertyWithValue("name", "userId-入参4");
    }

    @Test
    public void param5() {
        UserPageVi vi = new UserPageVi();
        vi.setUid("param5");
        vi.setName("入参5");
        UserInfo userInfo = faceDemoService.param5("userId", vi, 1);
        assertThat(userInfo).isNotNull().hasFieldOrPropertyWithValue("name", "userId-param5-1");
    }

    @Test
    public void param6() {
        UserPageVi vi1 = new UserPageVi();
        vi1.setUid("param61");
        vi1.setName("入参61");
        
        UserPageVi vi2 = new UserPageVi();
        vi2.setUid("param62");
        vi2.setName("入参62");

        UserInfo userInfo = faceDemoService.param6(vi1, vi2, 1);
        assertThat(userInfo).isNotNull().hasFieldOrPropertyWithValue("name", "param61-入参62-1");
    }

    @Test
    public void param7() {
        UserPageVi vi1 = new UserPageVi();
        vi1.setUid("param61");
        vi1.setName("入参61");
        UserPageVi vi2 = new UserPageVi();
        vi2.setUid("param62");
        vi2.setName("入参62");
        Map<String, Object> map = new HashMap<>();
        map.put("mapKey1", "value1");
        map.put("mapKey2", "value2");
        
        UserInfo userInfo = faceDemoService.param7(vi1, vi2, 1, map);
        assertThat(userInfo).isNotNull().hasFieldOrPropertyWithValue("name", "入参61-param62-value2");
    }

    @Test
    public void param8() {
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

        UserInfo userInfo = faceDemoService.param8(map, vi1, vi2, 1, vi3);
        assertThat(userInfo).isNotNull().hasFieldOrPropertyWithValue("uid", "face");
    }

    @Test
    public void param9() {
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

        try {
            faceDemoService.param9(map, vi1, new Date(), 1, null, vi3);
        } catch ( Exception e ) {
            assertThat(e).as("参数异常提示").hasMessageContaining("姓名不能为空");
            System.out.println(e.getMessage());
        }
    }

}