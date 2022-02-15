package cc.bugcat.example.api;

import cc.bugcat.catclient.utils.CatClientBuilders;
import cc.bugcat.example.api.vi.UserPageVi;
import cc.bugcat.example.catclient.serverApi.Config;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FaceDemoServiceTest{

    private static FaceDemoService faceDemoService;

    static {
//

        Properties prop = new Properties();
        prop.put("core-server.remoteApi", "http://127.0.0.1:8012");

        Map<Class, Object> configMap = CatClientBuilders.builder(Config.class, FaceDemoService.class)
                .environment(prop)
                .build();

        faceDemoService = (FaceDemoService) configMap.get(FaceDemoService.class);

    }



    @Test
    public void param0() throws Exception {
        faceDemoService.param0();
    }

    @Test
    public void param1() throws Exception {
        faceDemoService.param1("userId");
    }

    @Test
    public void param2() throws Exception {
        faceDemoService.param2("userId", 1);
    }

    @Test
    public void param3() throws Exception {
        UserPageVi vi = new UserPageVi();
        vi.setUid("param3");
        vi.setName("入参3");
        faceDemoService.param3(vi);
    }

    @Test
    public void param4() throws Exception {
        UserPageVi vi = new UserPageVi();
        vi.setUid("param4");
        vi.setName("入参4");
        faceDemoService.param4("userId", vi);
    }

    @Test
    public void param5() throws Exception {
        UserPageVi vi = new UserPageVi();
        vi.setUid("param5");
        vi.setName("入参5");
        faceDemoService.param5("userId", vi, 1);
    }

    @Test
    public void param6() throws Exception {
        UserPageVi vi1 = new UserPageVi();
        vi1.setUid("param61");
        vi1.setName("入参61");
        UserPageVi vi2 = new UserPageVi();
        vi2.setUid("param62");
        vi2.setName("入参62");
        faceDemoService.param6(vi1, vi2, 1);
    }

    @Test
    public void param7() throws Exception {
        UserPageVi vi1 = new UserPageVi();
        vi1.setUid("param61");
        vi1.setName("入参61");
        UserPageVi vi2 = new UserPageVi();
        vi2.setUid("param62");
        vi2.setName("入参62");
        Map<String, Object> map = new HashMap<>();
        map.put("mapKey1", "value1");
        map.put("mapKey2", "value2");
        faceDemoService.param7(vi1, vi2, 1, map);
    }

    @Test
    public void param8() throws Exception {
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

        faceDemoService.param8(map, vi1, vi2, 1, vi3);
    }

    @Test
    public void param9() throws Exception {
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

        faceDemoService.param9(map, vi1, new Date(), 1, null, vi3);
    }

}