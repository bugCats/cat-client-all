package cc.bugcat.example.catclient.sign;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.annotation.CatMethod;
import cc.bugcat.catface.annotation.CatNote;
import cc.bugcat.example.dto.DemoDTO;
import cc.bugcat.example.tools.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * 
 * 需要签名版
 * 单元测试类 @link cc.bugcat.example.catclient.sign.SignRemoteTest
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
    @CatMethod(value = "/cat/sign1", notes = {@CatNote("needSign"), @CatNote(key = "EL", value = "#{nomalCtrl.out(\"${demo.apikey}\", demo.userkey)}"),
            @CatNote(key = "spring", value = "${demo.apikey}"), @CatNote(key = "spEL", value = "#{demo.userkey}")}, method = RequestMethod.POST)
    ResponseEntity<String> demo12(@ModelAttribute("demo") DemoDTO demo);

    /**
     * 密钥是可变的，通过方法上的参数获取
     * */
    @CatMethod(value = "/cat/sign2", notes = {@CatNote("needSign"), @CatNote(key = "EL", value = "#{tools.out(\"${demo.apikey}\", demo.userkey)}"),
                                                @CatNote(key = "spring", value = "${demo.apikey}"), 
                                                @CatNote(key = "spEL", value = "#{demo.userkey}")}, method = RequestMethod.POST)
    ResponseEntity<String> demo13(@CatNote("demo") @RequestBody DemoDTO demo);




    /**
     * 还可以使用 ThreadLocal、或者SendProcessor本身 传递密钥
     * 不再列举
     * */
    @CatMethod(value = "/cat/sign1", notes = @CatNote("needSign"), method = RequestMethod.POST)
    ResponseEntity<String> demo13(SignSendProcessor sender, @ModelAttribute("demo") DemoDTO demo);



}
