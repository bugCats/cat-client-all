package cc.bugcat.example.catclient.remote;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.annotation.CatMethod;
import cc.bugcat.catclient.annotation.CatNote;
import cc.bugcat.catclient.handler.CatSendProcessor;
import cc.bugcat.example.dto.Demo;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * 呆毛2，异常plus版
 * 单元测试类 {@link cc.bugcat.example.catclient.remote.ApiRemote2Test}
 *
 * @author: bugcat
 * */
@CatClient(host = "${core-server.remoteApi}", fallback = ApiRemote2Error.class)
public interface ApiRemote2 {

    /**
     * ApiRemote2Error 异常处理类
     *
     * 在呆毛1 ApiRemote1.demo9 可以看到，如果发生了http异常（40x、50x等），有一个全局的异常提示
     *
     * 如果需要精确到指定的方法、返回不同的异常提示，可以采用异常plus版
     *
     * fallback = ApiRemote2Error.class
     *
     * 其中，ApiRemote2Error 必须实现了 ApiRemote2 接口
     *
     * */


    @CatMethod(value = "/cat/demo21", method = RequestMethod.POST)
    ResponseEntity<Demo> demo1(@CatNote("req") @RequestBody Demo req);


    @CatMethod(value = "/cat/demo22", method = RequestMethod.POST)
    String demo2(CatSendProcessor send, Demo req);


    @CatMethod(value = "/cat/demo29", method = RequestMethod.GET)
    ResponseEntity<String> demo9(PageInfo<Demo> pageInfo);


}
