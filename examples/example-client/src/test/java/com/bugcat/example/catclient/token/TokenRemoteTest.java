package com.bugcat.example.catclient.token;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.alibaba.fastjson.JSONObject;
import com.bugcat.catclient.utils.CatClientUtil;
import com.bugcat.example.dto.Demo;
import com.bugcat.example.tools.ResponseEntity;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class TokenRemoteTest {

    private static TokenRemote remote;
    static {
        ((Logger) LoggerFactory.getLogger("ROOT")).setLevel(Level.ERROR);


        TokenFactory factory = new TokenFactory();
        CatClientUtil.registerBean(TokenFactory.class, factory);
        
        Properties prop = new Properties();
        prop.put("core-server.remoteApi", "http://127.0.0.1:8012");
        prop.put("demo.username", "bugcat");
        prop.put("demo.pwd", "[密码]");
        remote = CatClientUtil.proxy(TokenRemote.class, prop);
    }

    
    
    @Test
    public void token() throws Exception {
        Demo demo = new Demo();
        demo.setName("bugcat");
        demo.setMark("猫脸");
        ResponseEntity<String> token1 = remote.token1(demo);
        System.out.println("remote.token1=" + token1.getData());
        
        String token = "TokenRemoteTest-token2";
        ResponseEntity<String> token2 = remote.token2(demo, token);
        System.out.println("remote.token2=" + token2.getData());
        
        String url = "/cat/token";
        ResponseEntity<String> token3 = remote.token3(url, "TokenRemoteTest-token3", JSONObject.toJSONString(demo));
        System.out.println("remote.token3=" + token3.getData());

    }

}