package com.bugcat.example.catclient.remote;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.alibaba.fastjson.JSONObject;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.catclient.utils.CatClientUtil;
import com.bugcat.example.dto.Demo;
import com.bugcat.example.dto.PageInfo;
import com.bugcat.example.dto.ResponseEntity;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Properties;


/**
 * 异常增强版
 * */
public class ApiRemote2Test {



    static Properties prop = new Properties();
    static {
        ((Logger) LoggerFactory.getLogger("ROOT")).setLevel(Level.ERROR);
        prop.put("demo.remoteApi", "http://127.0.0.1:8010");
    }
    static ApiRemote2 remote = CatClientUtil.proxy(ApiRemote2.class, prop);


    @Test
    public void demo1() throws Exception {
        Demo demo = creart();
        ResponseEntity<Demo> resp = remote.demo1(demo);
        System.out.println(JSONObject.toJSONString(resp));

    }

    @Test
    public void demo2() throws Exception {
        Demo demo = creart();
        SendProcessor sendHandler = new SendProcessor();
        String resp = remote.demo2(sendHandler, demo);

        System.out.println("resp=" + JSONObject.toJSONString(resp));

        System.out.println("req=" + sendHandler.getReqStr());
        System.out.println("resp=" + sendHandler.getRespStr());
    }

 

    @Test
    public void demo9() throws Exception {
        PageInfo<Demo> pageInfo = new PageInfo<>();
        ResponseEntity<String> resp = remote.demo9(pageInfo);
        System.out.println(JSONObject.toJSONString(resp));
    }


    private Demo creart(){
        Demo demo = new Demo();
        demo.setId(System.currentTimeMillis());
        demo.setName("bug猫");
        demo.setMark("调用");
        return demo;
    }
    
}