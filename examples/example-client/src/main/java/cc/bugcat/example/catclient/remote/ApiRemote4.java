package cc.bugcat.example.catclient.remote;

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
 * 呆毛4，继承
 *
 * 注意，@CatClient 注解不在这里，而是在子代类上
 *
 * 单元测试类 {@link cc.bugcat.example.catclient.remote.ApiRemote4Test}
 *
 * @author: bugcat
 * */
@CatResponesWrapper(ResponseEntityWrapper.class)
public interface ApiRemote4 {


    /**
     * 将req序列化成json，再使用post发送字符串。@RequestBody 不能少
     *
     * @param req 入参
     * */
    @CatMethod(value = "/cat/demo1", method = RequestMethod.POST)
    ResponseEntity<Demo> demo1(@RequestBody Demo req);


    /**
     * @throws Exception  这个会提示异常，interface上包含了@CatResponesWrapper，意思是希望自动拆包装器类，
     *          但是实际上服务端返回的数据类型是 ResponseEntity<Demo>
     * */
    @CatMethod(value = "/cat/demo2", method = RequestMethod.POST)
    String demo2(CatSendProcessor send, @ModelAttribute("req") DemoEntity req);


    @CatMethod(value = "/cat/demo3", method = RequestMethod.GET, connect = 60000)
    ResponseEntity<PageInfo<Demo>> demo3(Demo req, CatSendProcessor send);


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

    @CatMethod(value = "/cat/demo7", method = RequestMethod.GET)
    Demo demo7(@RequestParam("name") String name);


    /**
     * 模拟发生http异常
     * */
    @CatMethod(value = "/cat/demo9", method = RequestMethod.GET)
    ResponseEntity<String> demo9();


}
