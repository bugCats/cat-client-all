package cc.bugcat.example.api;

import cc.bugcat.example.api.vo.UserInfo;
import cc.bugcat.example.tools.ResponseEntity;
import io.swagger.annotations.Api;

@Api(tags = "Catface - ParentInter")
public interface ParentInter {
    
    
    
    ResponseEntity<UserInfo> parentMethod1(String uid);



    UserInfo parentMethod2(UserInfo info);
    
}
