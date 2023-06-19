package cc.bugcat.example.api;

import cc.bugcat.catclient.handler.CatClientContextHolder;
import cc.bugcat.catface.annotation.CatNote;
import cc.bugcat.catface.annotation.CatNotes;
import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.example.api.vi.UserPageVi;
import cc.bugcat.example.api.vo.UserInfo;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;
import cc.bugcat.example.tools.ResponseEntityWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 精简模式。
 * 所有的方法均视为API，全是采用 post + Json 方式发起请求；
 * 方法名可以任意，但是不能重复！！
 * */
@Api(tags = "Catface - 精简模式")
@Catface
@CatResponesWrapper(ResponseEntityWrapper.class)
public interface FaceDemoService{

    
    UserInfo param0();

    UserInfo param1(@NotBlank(message = "userId不能为空") String userId);

    @ApiOperation("api - param2")
    UserInfo param2(String userId, Integer status);

    @CatNotes(value = {@CatNote(key = "uname", value = "#{user.name}")}, scope = CatNotes.Scope.Cilent)
    @CatNotes(value = {@CatNote(key = "uname", value = "#{user.nam}")}, scope = CatNotes.Scope.Server)
    UserInfo param3(@CatNote("user") UserPageVi vi);

    UserInfo param4(String userId, UserPageVi vi);

    UserInfo param5(String userId, UserPageVi vi, @CatNote("isStatus")  Boolean status);

    UserInfo param6(@CatNote("vi1") UserPageVi vi1, @CatNote("vi2") UserPageVi vi2, Integer status);

    UserInfo param7(UserPageVi vi1, UserPageVi vi2, Integer status, Map<String, Object> map);

    UserInfo param8(@ApiParam("参数map") Map<String, Object> map,
                    @ApiParam("参数vi1") @Valid UserPageVi vi1,
                    @ApiParam("参数vi2") UserPageVi vi2,
                    @ApiParam("参数status") @NotNull(message = "status 不能为空") @CatNote("status") Boolean status,
                    @ApiParam("参数vi3") @Valid ResponseEntity<PageInfo<UserPageVi>> vi3);

    default UserInfo param9(@ApiParam("参数map") Map<String, Object> map, 
                    @ApiParam("参数vi1") @Validated UserPageVi vi1,
                    @ApiParam("参数date") Date date,
                    @ApiParam("参数status") Integer status,
                    @ApiParam("参数decimal") BigDecimal decimal,
                    @ApiParam("参数vi3") @Valid ResponseEntity<PageInfo<UserPageVi>> vi3) {
        CatClientContextHolder holder = CatClientContextHolder.getContextHolder();
        Throwable exception = holder.getException();
        System.out.println("异常：" + exception.getMessage());
        return null;
    }
    
}
