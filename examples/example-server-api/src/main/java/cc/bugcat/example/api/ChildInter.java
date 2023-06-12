package cc.bugcat.example.api;


import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.example.api.vo.UserInfo;
import cc.bugcat.example.tools.ResponseEntity;
import cc.bugcat.example.tools.ResponseEntityWrapper;
import io.swagger.annotations.Api;
import org.springframework.stereotype.Component;

/**
 * 服务端CatServer继承，实现API升级
 * */

@Api(tags = "用户操作api")
@Catface
@CatResponesWrapper(ResponseEntityWrapper.class)
public interface ChildInter extends ParentInter {


    void childMethod4(UserInfo info);


    UserInfo childMethod5(String uid);
    
}
