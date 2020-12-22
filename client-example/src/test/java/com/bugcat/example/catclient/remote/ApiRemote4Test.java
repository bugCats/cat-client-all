package com.bugcat.example.catclient.remote;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.alibaba.fastjson.JSONObject;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.catclient.spi.CatDefaultConfiguration;
import com.bugcat.catclient.utils.CatToosUtil;
import com.bugcat.example.dto.Demo;
import com.bugcat.example.dto.DemoEntity;
import com.bugcat.example.dto.PageInfo;
import com.bugcat.example.dto.ResponseEntity;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Properties;


/**
 * 
 * */
public class ApiRemote4Test {
    
    
    static Properties prop = new Properties();
    static {
        ((Logger) LoggerFactory.getLogger("ROOT")).setLevel(Level.ERROR);
        CatDefaultConfiguration configuration = new CatDefaultConfiguration(){
            @Override
            public int socket() {
                return 30000;
            }

            @Override
            public int connect() {
                return 30000;
            }
        };
//        prop.put(CatDefaultConfiguration.class, configuration);
        prop.put("demo.remoteApi", "http://127.0.0.1:8010");
    }
    static ApiRemote4Ext remote = CatToosUtil.proxy(ApiRemote4Ext.class, prop);
    
    
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
        String resp = remote.demo2(sendHandler, new DemoEntity(demo));
        
        System.out.println("resp=" + JSONObject.toJSONString(resp));
        
        System.out.println("req=" + sendHandler.getReqStr());
        System.out.println("resp=" + sendHandler.getRespStr());
    }

    @Test
    public void demo3() throws Exception {
        Demo demo = creart();
        SendProcessor sendHandler = new SendProcessor();

        ResponseEntity<PageInfo<Demo>> resp = remote.demo3(demo, sendHandler);
        System.out.println("第一次=" + JSONObject.toJSONString(resp));

        demo.setId(3L);
        resp = remote.demo3(demo, sendHandler);
        System.out.println("第二次=" + JSONObject.toJSONString(resp));
    }

    
    @Test
    public void demo4() throws Exception {
        ResponseEntity<Demo> resp = remote.demo4("bug猫", "bug猫");
        System.out.println(JSONObject.toJSONString(resp));
        ResponseEntity<Demo> resp2 = remote.demo4("bug猫2", "bug猫2");
        System.out.println(JSONObject.toJSONString(resp2));
    }

    @Test
    public void demo5() throws Exception {
        Demo resp1 = remote.demo5(System.currentTimeMillis());
        System.out.println(JSONObject.toJSONString(resp1));
        
        Demo resp2 = remote.demo5(System.currentTimeMillis());
        System.out.println(JSONObject.toJSONString(resp2));
        
    }

    @Test
    public void demo6() throws Exception {
        SendProcessor sendHandler = new SendProcessor();
        Void nul = remote.demo6(System.currentTimeMillis(), sendHandler, "bug猫");
    }

    @Test
    public void demo9() throws Exception {
        ResponseEntity<String> resp = remote.demo9();
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