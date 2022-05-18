package cc.bugcat.example.api;

import cc.bugcat.example.api.vo.UserInfo;
import cc.bugcat.example.tools.ResponseEntity;

public interface ParentInter {
    
    
    
    ResponseEntity<UserInfo> parentMethod1(String uid);



    UserInfo parentMethod2(UserInfo info);
    
}
