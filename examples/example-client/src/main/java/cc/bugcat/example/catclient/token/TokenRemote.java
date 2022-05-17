package cc.bugcat.example.catclient.token;

import cc.bugcat.catclient.annotation.CatClient;
import cc.bugcat.catclient.annotation.CatMethod;
import cc.bugcat.catface.annotation.CatNote;
import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.example.dto.Demo;
import cc.bugcat.example.tools.ResponseEntity;
import cc.bugcat.example.tools.ResponseEntityWrapper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * 需要token验证版
 * 单元测试类 @cc.bugcat.example.catclient.token.TokenRemoteTest
 *
 * @author: bugcat
 * */
@CatResponesWrapper(ResponseEntityWrapper.class)
@CatClient(host = "${core-server.remoteApi}", interceptor = TokenInterceptor.class, connect = 3000, socket = 3000)
public interface TokenRemote {

    
    /**
     * 通过账户密码，获取token
     * 其中 username、pwd从环境配置中获取
     * */
    @CatMethod(value = "/cat/getToken", method = RequestMethod.POST,
            notes = {@CatNote(key = "username", value = "${demo.username}"), @CatNote(key = "pwd", value = "${demo.pwd}")})
    default ResponseEntity<String> getToken(CatSendProcessor sender, @RequestParam("username") String username, @RequestParam("pwd") String pwd) {
        return ResponseEntity.fail("-1", "当前网络异常！");
    }

    /**
     * {@code @CatNote}标记这个接口，需要token
     * */
    @CatMethod(value = "/cat/token", method = RequestMethod.POST, notes = @CatNote("needToken"))
    ResponseEntity<String> sendDemo1(@RequestBody Demo demo);

    /**
     * 将token作为请求头参数
     * */
    @CatMethod(value = "/cat/token", method = RequestMethod.POST)
    ResponseEntity<String> sendDemo2(@RequestBody Demo demo, @RequestHeader("token") String token);

    /**
     * 动态url，具体访问地址，有参数url确定
     * */
    @CatMethod(value = "{url}", method = RequestMethod.POST)
    default ResponseEntity<String> sendDemo3(@PathVariable("url") String url, @RequestHeader("token") String token, @RequestBody String req) {
        return ResponseEntity.fail("-1", "默认异常！");
    }

}
