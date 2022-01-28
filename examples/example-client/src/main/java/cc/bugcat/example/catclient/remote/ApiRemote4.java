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
 * 单元测试类 @link cc.bugcat.example.catclient.remote.ApiRemote4Test
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
     * 仅将req转换成键值对，再使用post发送键值对。
     *
     * @param send  请求协助类，必须是SendProcessor的子类；可无、位置可任意
     * @param req   对象的属性，不能有Map
     * @throws Exception  这个会提示异常，class上包含了@CatResponesWrapper，意思是希望自动拆包装器类，
     *          但是实际上服务端返回的数据类型是 ResponseEntity<Demo>
     * */
    @CatMethod(value = "/cat/demo2", method = RequestMethod.POST)
    String demo2(CatSendProcessor send, @ModelAttribute("req") DemoEntity req);


    /**
     * 仅将req转换成键值对，再使用get发送键值对。为此方法单独设置了链接超时为60s
     *
     * @param req   对象的属性，不能有Map
     * @param send  请求协助类，必须是SendProcessor的子类；可无、位置可任意（对比demo2）
     * */
    @CatMethod(value = "/cat/demo3", method = RequestMethod.GET, connect = 60000)
    ResponseEntity<PageInfo<Demo>> demo3(Demo req, CatSendProcessor send);


    /**
     * get发送键值对：name=aaa&mark=bbb
     * @param name 参数。必须需要使用@RequestParam指定参数的名称
     * @param mark 参数
     * */
    @CatMethod(value = "/cat/demo4", method = RequestMethod.GET)
    ResponseEntity<Demo> demo4(@RequestParam("name") String name, @RequestParam("mark") String mark);


    /**
     * 将参数拼接在url上
     *
     * @param userId url上的参数，同样必须使用@PathVariable指定参数的名称，参数位置随意
     * @throws Exception  这个会提示异常，class上包含了@CatResponesWrapper，意思是希望自动拆包装器类，
     *          但是实际上服务端返回的数据类型是 Demo
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
    Void demo6(@PathVariable("userId") Long userId, CatSendProcessor send, @RequestParam("name") String name);


    /**
     * 正常拆包装器类
     * @param name 键值对参数
     * */
    @CatMethod(value = "/cat/demo7", method = RequestMethod.GET)
    Demo demo7(@RequestParam("name") String name);

    /**
     * 模拟发生http异常
     * */
    @CatMethod(value = "/cat/demo9", method = RequestMethod.GET)
    ResponseEntity<String> demo9();


}
