package cc.bugcat.example.catclient.remote;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.annotation.CatMethod;
import cc.bugcat.catclient.annotation.CatNote;
import cc.bugcat.catclient.handler.CatSendProcessor;
import cc.bugcat.example.dto.Demo;
import cc.bugcat.example.dto.DemoEntity;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *
 * 示例1，基础版
 *
 * 需要启动 example-server 系统，调用 cc.bugcat.example.catclient.ApiRemoteController
 *
 * 单元测试类 {@link cc.bugcat.example.catclient.remote.ApiRemote1Test}
 *
 * @author: bugcat
 * */
@CatClient(host = "${core-server.remoteApi}")
public interface ApiRemoteService1 {



    /**
     * 将req序列化成json，再使用post发送字符串。@RequestBody 不能少。
     * 如果要给入参req取别名，使用{@link CatNote}：@CatNote("req")
     *
     * @param req 入参
     * */
    @CatMethod(value = "/cat/demo1", method = RequestMethod.POST)
    ResponseEntity<Demo> demo1(@RequestBody Demo req);



    /**
     * 仅将req转换成键值对，再使用post发送键值对。
     *
     * @param send  http请求发送类，必须是SendProcessor的子类；可无、位置可任意
     * @param req
     * */
    @CatMethod(value = "/cat/demo2", method = RequestMethod.POST)
    String demo2(CatSendProcessor send, @ModelAttribute("req") DemoEntity req);



    /**
     * 仅将req转换成键值对，再使用get发送键值对。为此方法单独设置了链接超时为60s
     * 对象不加@ModelAttribute、@RequestParam、@PathVariable、@RequestBody等注解时，默认是按{@code @ModelAttribute}键值对发送
     *
     * @param req
     * @param send  http请求发送类，必须是SendProcessor的子类；可无、位置可任意（对比demo2）
     * */
    @CatMethod(value = "/cat/demo3", method = RequestMethod.GET, connect = 60000)
    ResponseEntity<PageInfo<Demo>> demo3(Demo req, CatSendProcessor send);


    /**
     * get发送键值对：name=aaa&mark=bbb
     *
     * @param name 参数。必须需要使用@RequestParam指定参数的名称
     * @param mark 参数
     * */
    @CatMethod(value = "/cat/demo4", method = RequestMethod.GET)
    ResponseEntity<Demo> demo4(@RequestParam("name") String name, @RequestParam("mark") String mark);


    /**
     * 将参数拼接在url上
     *
     * @param userId url上的参数，同样必须使用@PathVariable指定参数的名称，参数位置随意
     * */
    @CatMethod(value = "/cat/demo5/{userId}", method = RequestMethod.GET)
    Demo demo5(@PathVariable("userId") Long userId);




    /**
     * 将参数拼接在url上，并且有http请求发送类，并且使用get键值对发送请求
     *
     * @param userId url上的参数
     * @param send http请求发送类
     * @param name 键值对参数
     * */
    @CatMethod(value = "/cat/demo6/{userId}", method = RequestMethod.GET)
    Void demo6(@PathVariable("userId") Long userId, CatSendProcessor send, @RequestParam("name") String name);



    /**
     * 将参数拼接在url上，并且还有header参数
     *
     * @param userId url上的参数，同样必须使用@PathVariable指定参数的名称，参数位置随意
     * @param token 请求头中的参数
     * */
    @CatMethod(value = "/cat/demo5/{userId}", method = RequestMethod.GET)
    Demo demo7(@PathVariable("userId") Long userId, @RequestHeader("token") String token);



    /**
     * 模拟发生http异常
     * */
    @CatMethod(value = "/cat/demo9", method = RequestMethod.GET)
    ResponseEntity<String> demo9();


}
