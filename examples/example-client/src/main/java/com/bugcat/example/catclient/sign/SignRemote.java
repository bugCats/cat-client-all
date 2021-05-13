package com.bugcat.example.catclient.sign;

import com.bugcat.catclient.annotation.CatClient;
import com.bugcat.catclient.annotation.CatMethod;
import com.bugcat.catclient.annotation.CatNote;
import com.bugcat.example.dto.DemoDTO;
import com.bugcat.example.tools.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * 
 * 需要签名版
 * 单元测试类 @link com.bugcat.example.catclient.sign.SignRemoteTest
 *
 * @author: bugcat
 * */
@CatClient(host = "${core-server.remoteApi}", factory = SignFactory.class, connect = 3000, socket = 3000)
public interface SignRemote {
    

    
    /**
     * 密钥是固定的，通过配置文件获取
     * */
    @CatMethod(value = "/cat/sign1", notes = {@CatNote("needSign"), @CatNote(key = "apikey", value = "${demo.apikey}")}, method = RequestMethod.POST)
    ResponseEntity<String> demo11(Map<String, Object> param);




    /**
     * 密钥是可变的，通过方法上的参数获取
     * */
    @CatMethod(value = "/cat/sign1", notes = {@CatNote("needSign"), @CatNote(key = "apikey", value = "#{demo.userkey}")}, method = RequestMethod.POST)
    ResponseEntity<String> demo12(@ModelAttribute("demo") DemoDTO demo);
    
    
    
    /**
     * 还可以使用 ThreadLocal、或者SendProcessor本身 传递密钥
     * 不再列举
     * */
    @CatMethod(value = "/cat/sign1", notes = @CatNote("needSign"), method = RequestMethod.POST)
    ResponseEntity<String> demo13(SignSendProcessor sender, @ModelAttribute("demo") DemoDTO demo);



}
