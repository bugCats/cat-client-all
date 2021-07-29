package cc.bugcat.example.catclient.remote;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.annotation.CatMethod;
import cc.bugcat.catclient.handler.SendProcessor;
import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.example.dto.DemoEntity;
import cc.bugcat.example.dto.Demo;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntityWrapper;
import org.springframework.web.bind.annotation.*;

/**
 * 
 * 呆毛3，去包装器类版
 * 单元测试类 @link cc.bugcat.example.catclient.remote.ApiRemote3Test
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
 * @author: bugcat
 * */
@CatResponesWrapper(ResponseEntityWrapper.class)
@CatClient(host = "${core-server.remoteApi}")
public interface ApiRemote3 {


    /**
     * 
     * 对比 ApiRemote1，方法响应都没有ResponseEntity了
     * 
     * */

    
    
    /**
     * 将req序列化成json，再使用post发送字符串。@RequestBody 不能少
     *
     * @param req 入参
     * */
    @CatMethod(value = "/cat/demo1", method = RequestMethod.POST)
    Demo demo1(@RequestBody Demo req);


    /**
     * 仅将req转换成键值对，再使用post发送键值对。
     *
     * @param send  请求协助类，必须是SendProcessor的子类；可无、位置可任意
     * @param req   对象的属性，不能有Map
     * */
    @CatMethod(value = "/cat/demo2", method = RequestMethod.POST)
    String demo2(SendProcessor send, @ModelAttribute("req") DemoEntity req);


    /**
     * 仅将req转换成键值对，再使用get发送键值对。为此方法单独设置了链接超时为60s
     *
     * @param req   对象的属性，不能有Map
     * @param send  请求协助类，必须是SendProcessor的子类；可无、位置可任意（对比demo2）
     * */
    @CatMethod(value = "/cat/demo3", method = RequestMethod.GET, connect = 60000)
    PageInfo<Demo> demo3(Demo req, SendProcessor send);


    /**
     * get发送键值对：name=aaa&mark=bbb
     * @param name 参数。必须需要使用@RequestParam指定参数的名称
     * @param mark 参数
     * */
    @CatMethod(value = "/cat/demo4", method = RequestMethod.GET)
    Demo demo4(@RequestParam("name") String name, @RequestParam("mark") String mark);


    /**
     * 将参数拼接在url上
     *
     * @param userId url上的参数，同样必须使用@PathVariable指定参数的名称，参数位置随意
     * @throws Exception 这个会提示异常，class上包含了@CatResponesWrapper，意思是希望通过 ResponseEntityWrapper 拆包装器类
     *                  但是实际上，服务端仅返回了 Demo 对象 
     * */
    @CatMethod(value = "/cat/demo5/{userId}", method = RequestMethod.GET)
    Demo demo5(@PathVariable("userId") Long userId);


    /**
     * 将参数拼接在url上，并且有请求协助类，并且使用get键值对发送请求
     *
     * @param userId url上的参数
     * @param send 请求协助类
     * @param name 键值对参数
     * */
    @CatMethod(value = "/cat/demo6/{userId}", method = RequestMethod.GET)
    Void demo6(@PathVariable("userId") Long userId, SendProcessor send, @RequestParam("name") String name);


    /**
     * 模拟发生http异常
     * */
    @CatMethod(value = "/cat/demo9", method = RequestMethod.GET)
    String demo9();



}
