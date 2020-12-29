package com.bugcat.example.catclient.remote;

import com.bugcat.catclient.annotation.CatMethod;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.catface.annotation.CatResponesWrapper;
import com.bugcat.example.dto.Demo;
import com.bugcat.example.dto.DemoEntity;
import com.bugcat.example.tools.PageInfo;
import com.bugcat.example.tools.ResponseEntity;
import com.bugcat.example.tools.ResponseEntityWrapper;
import org.springframework.web.bind.annotation.*;

/**
 * 
 * 呆毛4，继承
 * 
 * 注意，@CatClient 注解不在这里，而是在子代类上
 * 
 * 单元测试类 @link com.bugcat.example.catclient.remote.ApiRemote4Test
 *
 * @author: bugcat
 * */
@CatResponesWrapper(ResponseEntityWrapper.class)
public interface ApiRemote4 {
    

    /**
     * 将req序列化成json，再使用post发送json字符串。@RequestBody 不能少
     * 
     * @param req 入参
     * */
    @CatMethod(value = "/server/demo1", method = RequestMethod.POST)
    ResponseEntity<Demo> demo1(@RequestBody Demo req);


    /**
     * 仅将req转换成键值对，再使用post发送键值对。
     * 
     * @param send  请求协助类，必须是SendProcessor的子类；可无、位置可任意
     * @param req   对象的属性，不能有Map
     * */
    @CatMethod(value = "/server/demo2", method = RequestMethod.POST)
    String demo2(SendProcessor send, @ModelAttribute("req") DemoEntity req);


    /**
     * 仅将req转换成键值对，再使用get发送键值对。为此方法单独设置了链接超时为60s
     *
     * @param req   对象的属性，不能有Map
     * @param send  请求协助类，必须是SendProcessor的子类；可无、位置可任意（对比demo2）
     * */
    @CatMethod(value = "/server/demo3", method = RequestMethod.GET, connect = 60000)
    ResponseEntity<PageInfo<Demo>> demo3(Demo req, SendProcessor send);
   

    /**
     * get发送键值对：name=aaa&mark=bbb
     * @param name 参数。必须需要使用@RequestParam指定参数的名称
     * @param mark 参数
     * */
    @CatMethod(value = "/server/demo4", method = RequestMethod.GET)
    ResponseEntity<Demo> demo4(@RequestParam("name") String name, @RequestParam("mark") String mark);


    /**
     * 将参数拼接在url上
     * 
     * @param userId url上的参数，同样必须使用@PathVariable指定参数的名称，参数位置随意
     * */
    @CatMethod(value = "/server/demo5/{userId}", method = RequestMethod.GET)
    Demo demo5(@PathVariable("userId") Long userId);


    /**
     * 将参数拼接在url上，并且有请求协助类，并且使用get键值对发送请求
     * 
     * @param userId url上的参数
     * @param send 请求协助类
     * @param name 键值对参数
     * */
    @CatMethod(value = "/server/demo6/{userId}", method = RequestMethod.GET)
    Void demo6(@PathVariable("userId") Long userId, SendProcessor send, @RequestParam("name") String name);

    
    /**
     * 模拟发生http异常
     * */
    @CatMethod(value = "/server/demo9", method = RequestMethod.GET)
    ResponseEntity<String> demo9();
    
    
}
