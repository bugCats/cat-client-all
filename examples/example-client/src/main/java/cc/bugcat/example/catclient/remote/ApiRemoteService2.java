package cc.bugcat.example.catclient.remote;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.annotation.CatMethod;
import cc.bugcat.catclient.handler.CatSendContextHolder;
import cc.bugcat.catface.annotation.CatNote;
import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.example.dto.Demo;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * 呆毛2，异常plus版
 * 增加了fallback，如果发生http异常，会执行ApiRemoteService2Fallback对应的方法
 *
 * 单元测试类 {@link cc.bugcat.example.catclient.remote.ApiRemote2Test}
 *
 * @author: bugcat
 * */
@CatClient(host = "${core-server.remoteApi}", fallback = ApiRemoteService2Fallback.class)
public interface ApiRemoteService2 {

    /**
     * ApiRemoteService2Fallback 异常处理类
     *
     * 在呆毛1 ApiRemote1.demo9 可以看到，如果发生了http异常（40x、50x等），有一个全局的异常提示
     *
     * 如果需要精确到指定的方法、返回不同的异常提示，可以采用异常plus版
     *
     * fallback = ApiRemoteService2Fallback.class
     *
     * 其中，ApiRemoteService2Fallback 必须实现了 ApiRemoteService2 接口
     *
     * */


    @CatMethod(value = "/cat/demo21", method = RequestMethod.POST)
    default ResponseEntity<Demo> demo1(@CatNote("req") @RequestBody Demo req){
        CatSendContextHolder contextHolder = CatSendContextHolder.getContextHolder();
        Throwable exception = contextHolder.getException();
        return ResponseEntity.fail("-1", exception.getMessage());
    }


    @CatMethod(value = "/cat/demo22", method = RequestMethod.POST)
    default String demo2(CatSendProcessor send, Demo req){
        CatSendContextHolder contextHolder = CatSendContextHolder.getContextHolder();
        Throwable exception = contextHolder.getException();
        return "ApiRemoteService2.demo2默认方法 > " + exception.getMessage();
    }


    @CatMethod(value = "/cat/demo29", method = RequestMethod.GET)
    ResponseEntity<String> demo9(PageInfo<Demo> pageInfo);


}
