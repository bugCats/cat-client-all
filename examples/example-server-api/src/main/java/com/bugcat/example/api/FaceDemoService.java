package com.bugcat.example.api;

import com.bugcat.catface.annotation.CatResponesWrapper;
import com.bugcat.catface.annotation.Catface;
import com.bugcat.example.api.vi.UserPageVi;
import com.bugcat.example.api.vo.UserInfo;
import com.bugcat.example.tools.PageInfo;
import com.bugcat.example.tools.ResponseEntity;
import com.bugcat.example.tools.ResponseEntityWrapper;
import io.swagger.annotations.Api;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;


/**
 * 精简模式
 * */
@Api
@Catface
@CatResponesWrapper(ResponseEntityWrapper.class)
public interface FaceDemoService{


    UserInfo param0();

    UserInfo param1(String userId);
    
    UserInfo param2(String userId, @RequestParam(value = "status", required = false) Integer status);

    UserInfo param3(UserPageVi vi);

    UserInfo param4(String userId, UserPageVi vi);

    UserInfo param5(String userId, UserPageVi vi, Integer status);

    UserInfo param6(UserPageVi vi1, UserPageVi vi2, Integer status);

    UserInfo param7(UserPageVi vi1, UserPageVi vi2, Integer status, Map<String, Object> map);

    UserInfo param8(Map<String, Object> map, UserPageVi vi1, UserPageVi vi2, Integer status, ResponseEntity<PageInfo<UserPageVi>> vi3);

    UserInfo param9(Map<String, Object> map, @Validated UserPageVi vi1, 
                    Date date, Integer status, 
                    BigDecimal decimal,
                    @Valid ResponseEntity<PageInfo<UserPageVi>> vi3);


}
