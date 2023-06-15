package cc.bugcat.example.catserver.serverApi;

import cc.bugcat.catserver.annotation.CatServer;
import cc.bugcat.example.api.ChildInter;
import cc.bugcat.example.api.vo.UserInfo;
import cc.bugcat.example.tools.ResponseEntity;


@CatServer
public class CatFaceInherit implements ChildInter {

    @Override
    public void childMethod4(UserInfo info) {
        
    }

    @Override
    public UserInfo childMethod5(String uid) {
        return null;
    }

    @Override
    public ResponseEntity<UserInfo> parentMethod1(String uid) {
        return null;
    }

    @Override
    public UserInfo parentMethod2(UserInfo info) {
        return null;
    }
}
