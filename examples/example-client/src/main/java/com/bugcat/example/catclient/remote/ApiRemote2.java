package com.bugcat.example.catclient.remote;

import com.bugcat.catclient.annotation.CatClient;
import com.bugcat.catclient.annotation.CatMethod;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.example.dto.Demo;
import com.bugcat.example.tools.PageInfo;
import com.bugcat.example.tools.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 
 * 呆毛2，异常plus版
 * 单元测试类 @link com.bugcat.example.catclient.remote.ApiRemote2Test
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
    
    
    /**
     * 将req序列化成json，再使用post发送字符串。@RequestBody 不能少
     * 
     * @param req 入参
     * */
    @CatMethod(value = "/cat/demo21", method = RequestMethod.POST)
    ResponseEntity<Demo> demo1(@RequestBody Demo req);


    /**
     * 仅将req转换成键值对，再使用post发送键值对。
     * 
     * @param send  请求协助类，必须是SendProcessor的子类；可无、位置可任意
     * @param req   对象的属性，不能有Map
     * */
    @CatMethod(value = "/cat/demo22", method = RequestMethod.POST)
    String demo2(SendProcessor send, Demo req);

    /**
     * 模拟发生http异常
     * */
    @CatMethod(value = "/cat/demo29", method = RequestMethod.GET)
    ResponseEntity<String> demo9(PageInfo<Demo> pageInfo);


}
