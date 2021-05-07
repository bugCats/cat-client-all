package com.bugcat.example.catclient.sign;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.bugcat.catclient.utils.CatClientUtil;
import com.bugcat.example.catclient.remote.ApiRemote3;
import com.bugcat.example.dto.DemoDTO;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * 需要签名的呆毛
 * */
public class SignRemoteTest {

    private static SignRemote remote;
    static {
        ((Logger) LoggerFactory.getLogger("ROOT")).setLevel(Level.ERROR);

        SignFactory factory = new SignFactory();
        CatClientUtil.registerBean(SignFactory.class, factory);
        
        Properties prop = new Properties();
        prop.put("demo.remoteApi", "http://127.0.0.1:8010");
        prop.put("demo.apikey", "bugcat");
        remote = CatClientUtil.proxy(SignRemote.class, prop);
    }
    
    
    @Test
    public void demo11() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "bugcat");
        map.put("project", "");
        map.put("mark", "签名");
        remote.demo11(map);// 对比控制台打印的签名sign字段
    }

    
    @Test
    public void demo12() throws Exception {
        DemoDTO demo = new DemoDTO();
        demo.setName("bugcat");
        demo.setMark("猫脸");
        demo.setUserkey("这是密钥");
        remote.demo12(demo);// 对比控制台打印的签名sign字段
    }
}