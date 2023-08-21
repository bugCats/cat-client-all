package cc.bugcat.example.catserver.serverApi;

import cc.bugcat.catserver.annotation.CatServer;
import cc.bugcat.example.api.FaceDemoService;
import cc.bugcat.example.api.vi.UserPageVi;
import cc.bugcat.example.api.vo.UserInfo;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;
import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;


@CatServer(interceptors = UserInterceptor.class) // 自定义拦截器+拦截器组
public class FaceDemoServiceImpl implements FaceDemoService {

    @Override
    public UserInfo param0() {
        System.out.println("param0:");
        return info("param0");
    }

    @Override
    public UserInfo param1(String userId) {
        System.out.println("param1: userId=" + userId);
        return info(userId);
    }

    @Override
    public UserInfo param2(String userId, Integer status) {
        System.out.println("param2: userId=" + userId + "; status=" + status);
        return info(userId + "-" + status);
    }

    @Override
    public UserInfo param3(UserPageVi vi) {
        System.out.println("param3: vi=" + JSONObject.toJSONString(vi));
        return info(vi.getUid() + "-" + vi.getName());
    }

    @Override
    public UserInfo param4(String userId, UserPageVi vi) {
        System.out.println("param4: userId=" + userId + ";vi=" + JSONObject.toJSONString(vi));
        return info(userId + "-" + vi.getName());
    }

    @Override
    public UserInfo param5(String userId, UserPageVi vi, Boolean status) {
        System.out.println("param5: userId=" + userId + ";vi=" + JSONObject.toJSONString(vi) + ";status=" + status);
        return info(userId + "-" + vi.getUid() + "-" + status);
    }

    @Override
    public UserInfo param6(UserPageVi vi1, UserPageVi vi2, Integer status) {
        System.out.println("param6: vi1=" + JSONObject.toJSONString(vi1) + ";vi2=" + JSONObject.toJSONString(vi2)
                        + ";status=" + status );
        return info(vi1.getUid() + "-" + vi2.getName() + "-" + status);
    }

    @Override
    public UserInfo param7(UserPageVi vi1, UserPageVi vi2, Integer status, Map<String, Object> map) {
        System.out.println("param7: vi1=" + JSONObject.toJSONString(vi1) + ";vi2=" + JSONObject.toJSONString(vi2)
                        + ";status=" + status + ";map=" + JSONObject.toJSONString(map));
        return info( vi1.getName() + "-" + vi2.getUid() + "-" + map.get("mapKey2"));
    }

    @Override
    public UserInfo param8(Map<String, Object> map, UserPageVi vi1, UserPageVi vi2, Boolean status, ResponseEntity<PageInfo<UserPageVi>> vi3) {
        System.out.println("param8: vi1=" + JSONObject.toJSONString(vi1) + ";map=" + JSONObject.toJSONString(map) + ";vi2=" + JSONObject.toJSONString(vi2)
                + ";vi3=" + JSONObject.toJSONString(vi3) + ";status=" + status );
        return info("param8");
    }


    @Override
    public UserInfo param9(Map<String, Object> map, UserPageVi vi1, Date date, Integer status, BigDecimal decimal, ResponseEntity<PageInfo<UserPageVi>> vi3) {
        System.out.println("param9: vi1=" + JSONObject.toJSONString(vi1) + ";map=" + JSONObject.toJSONString(map)
                + ";vi2=" + JSONObject.toJSONString(date)
                + ";vi3=" + JSONObject.toJSONString(vi3)
                + ";decimal=" + decimal
                + ";status=" + status );
        return info("param9");
    }


    private UserInfo info(String methodName){
        UserInfo info = new UserInfo();
        info.setUid("face");
        info.setName(methodName);
        return info;
    }
}
