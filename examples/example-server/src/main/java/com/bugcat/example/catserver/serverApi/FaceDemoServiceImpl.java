package com.bugcat.example.catserver.serverApi;

import com.alibaba.fastjson.JSONObject;
import com.bugcat.catserver.annotation.CatServer;
import com.bugcat.example.api.FaceDemoService;
import com.bugcat.example.api.vi.UserPageVi;
import com.bugcat.example.api.vi.UserSaveVi;
import com.bugcat.example.api.vo.UserInfo;
import com.bugcat.example.tools.PageInfo;
import com.bugcat.example.tools.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;


@CatServer
public class FaceDemoServiceImpl implements FaceDemoService{

    @Override
    public UserInfo param0() {
        System.out.println("param0:");
        return info();
    }

    @Override
    public UserInfo param1(String userId) {
        System.out.println("param1: userId=");
        return info();
    }

    @Override
    public UserInfo param2(String userId, Integer status) {
        System.out.println("param2: userId=" + userId + "; status=" + status);
        return info();
    }

    @Override
    public UserInfo param3(UserPageVi vi) {
        System.out.println("param3: vi=" + JSONObject.toJSONString(vi));
        return info();
    }

    @Override
    public UserInfo param4(String userId, UserPageVi vi) {
        System.out.println("param4: userId=" + userId + ";vi=" + JSONObject.toJSONString(vi));
        return info();
    }

    @Override
    public UserInfo param5(String userId, UserPageVi vi, Integer status) {
        System.out.println("param5: userId=" + userId + ";vi=" + JSONObject.toJSONString(vi) + ";status=" + status);
        return info();
    }

    @Override
    public UserInfo param6(UserPageVi vi1, UserPageVi vi2, Integer status) {
        System.out.println("param6: vi1=" + JSONObject.toJSONString(vi1) + ";vi2=" + JSONObject.toJSONString(vi2)
                        + ";status=" + status );
        return info();
    }

    @Override
    public UserInfo param7(UserPageVi vi1, UserPageVi vi2, Integer status, Map<String, Object> map) {
        System.out.println("param7: vi1=" + JSONObject.toJSONString(vi1) + ";vi2=" + JSONObject.toJSONString(vi2)
                        + ";status=" + status + ";map=" + JSONObject.toJSONString(map));
        return info();
    }

    @Override
    public UserInfo param8(Map<String, Object> map, UserPageVi vi1, UserPageVi vi2, Integer status, ResponseEntity<PageInfo<UserPageVi>> vi3) {
        System.out.println("param8: vi1=" + JSONObject.toJSONString(vi1) + ";map=" + JSONObject.toJSONString(map) + ";vi2=" + JSONObject.toJSONString(vi2)
                + ";vi3=" + JSONObject.toJSONString(vi3) + ";status=" + status );
        return info();
    }


    @Override
    public UserInfo param9(Map<String, Object> map, UserPageVi vi1, Date date, Integer status, BigDecimal decimal, ResponseEntity<PageInfo<UserPageVi>> vi3) {
        System.out.println("param9: vi1=" + JSONObject.toJSONString(vi1) + ";map=" + JSONObject.toJSONString(map)
                + ";vi2=" + JSONObject.toJSONString(date)
                + ";vi3=" + JSONObject.toJSONString(vi3)
                + ";decimal=" + decimal
                + ";status=" + status );
        return info();
    }

    private UserInfo info(){
        UserInfo info = new UserInfo();
        info.setUid("face");
        info.setName("ok");
        return info;
    }
}
