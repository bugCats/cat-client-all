package cc.bugcat.example.catclient.remote;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.annotation.CatMethod;
import cc.bugcat.catclient.handler.CatSendProcessor;
import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.example.dto.DemoEntity;
import cc.bugcat.example.dto.Demo;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;
import cc.bugcat.example.tools.ResponseEntityWrapper;
import org.springframework.web.bind.annotation.*;

/**
 *
 * 呆毛3，去包装器类版
 *
 * 单元测试类 {@link cc.bugcat.example.catclient.remote.ApiRemote3Test}
 *
 * ApiRemote1、ApiRemote2可以发现，API响应对象统一为 ResponseEntity，使用ResponseEntity将业务响应进行封装，
 *
 * 具体的业务参数，是ResponseEntity一个属性。
 *
 * 在执行方法之后，还需要根据 ResponseEntity.getErrCode() 判断是否执行成功，
 * ResponseEntity.getDate() 得到具体的业务数据
 *
 * 如果不想这么做，只想获取业务参数，如果发生业务异常，直接抛出就行
 * 可以采用去包装器类版。
 *
 *
 * 此时可以通过配置，实现统一去掉ResponseEntity外层
 *
 * @see CatResponesWrapper
 * @see ResponseEntityWrapper
 *
 * @author: bugcat
 * */
@CatResponesWrapper(ResponseEntityWrapper.class)
@CatClient(host = "${core-server.remoteApi}")
public interface ApiRemote3 {

    /**
     * 对比 ApiRemote1，部分方法响应没有ResponseEntity了, 部分响应改成String类型
     * */



    @CatMethod(value = "/cat/demo1", method = RequestMethod.POST)
    Demo demo1(@RequestBody Demo req);

    /**
     * @throws Exception  这个会提示异常，interface上包含了@CatResponesWrapper，意思是希望自动拆包装器类，
     *          但是实际上服务端返回的数据类型是 ResponseEntity<Demo>
     * */
    @CatMethod(value = "/cat/demo2", method = RequestMethod.POST)
    String demo2(CatSendProcessor send, @ModelAttribute("req") DemoEntity req);


    @CatMethod(value = "/cat/demo3", method = RequestMethod.GET, connect = 60000)
    PageInfo<Demo> demo3(Demo req, CatSendProcessor send);

    @CatMethod(value = "/cat/demo4", method = RequestMethod.GET)
    ResponseEntity<Demo> demo4(@RequestParam("name") String name, @RequestParam("mark") String mark);

    /**
     * @throws Exception  这个会提示异常，interface上包含了@CatResponesWrapper，意思是希望自动拆包装器类，
     *          但是实际上服务端返回的数据类型是 Demo
     * */
    @CatMethod(value = "/cat/demo5/{userId}", method = RequestMethod.GET)
    Demo demo5(@PathVariable("userId") Long userId);


    @CatMethod(value = "/cat/demo6/{userId}", method = RequestMethod.GET)
    Void demo6(@PathVariable("userId") Long userId, CatSendProcessor send, @RequestParam("name") String name);


    /**
     * 模拟发生http异常
     * */
    @CatMethod(value = "/cat/demo9", method = RequestMethod.GET)
    String demo9();



}
