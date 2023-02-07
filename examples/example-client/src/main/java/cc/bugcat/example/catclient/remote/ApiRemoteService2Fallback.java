package cc.bugcat.example.catclient.remote;

import cc.bugcat.catclient.handler.CatClientContextHolder;
import cc.bugcat.example.dto.Demo;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;

/**
 * 呆毛2，异常plus版回调类
 *
 * 异常回调类
 *
 * @author: bugcat
 * */
public class ApiRemoteService2Fallback implements ApiRemoteService2 {


    @Override
    public ResponseEntity<Demo> demo1(Demo req) {
        return ResponseEntity.fail("demo1", "demo1实际调用失败, 这是ApiRemote2Error异常回调类返回的");
    }


//    @Override
//    public String demo2(CatSendProcessor send, Demo req) {
//        CatClientContextHolder context = CatClientContextHolder.getContextHolder();
//        context.getException().printStackTrace(); //打印异常堆栈
//        return "demo2 异常. ApiRemote2Error异常回调类返回";
//    }



    @Override
    public ResponseEntity<String> demo9(PageInfo<Demo> pageInfo) {
        CatClientContextHolder contextHolder = CatClientContextHolder.getContextHolder();
        return ResponseEntity.fail("demo9", "这是真的异常了:" + contextHolder.getException().getMessage());
    }
}
