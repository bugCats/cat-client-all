package cc.bugcat.example.catclient.remote;

import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.handler.CatJacksonResolver;
import cc.bugcat.catclient.handler.CatSendProcessor;
import cc.bugcat.catclient.handler.CatHttpPoint;
import cc.bugcat.catclient.spi.CatJsonResolver;
import cc.bugcat.catclient.utils.CatClientBuilders;
import cc.bugcat.example.dto.Demo;
import cc.bugcat.example.dto.DemoEntity;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Properties;


/**
 *
 * */
public class ApiRemote4Test {


    private static ApiRemote4Ext remote;
    static {
        ((Logger) LoggerFactory.getLogger("ROOT")).setLevel(Level.ERROR);

        Properties prop = new Properties();
        prop.put("core-server.remoteApi", "http://127.0.0.1:8012");
        CatClientConfiguration configuration = new CatClientConfiguration(){
            @Override
            public int socket() {
                return 30000;
            }

            @Override
            public int connect() {
                return 30000;
            }

            @Override
            public CatJsonResolver jsonResolver() {
                return new CatJacksonResolver();
            }
        };
        prop.put(CatClientConfiguration.class, configuration);

        remote = CatClientBuilders.builder(ApiRemote4Ext.class)
                .environment(prop)
                .build();
    }





    @Test
    public void demo1() throws Exception {
        Demo demo = creart();
        ResponseEntity<Demo> resp = remote.demo1(demo);
        System.out.println(JSONObject.toJSONString(resp));

    }

    @Test
    public void demo2() throws Exception {
        Demo demo = creart();
        CatSendProcessor sendHandler = new CatSendProcessor();
        String resp = remote.demo2(sendHandler, new DemoEntity(demo));

        System.out.println("resp=" + JSONObject.toJSONString(resp));

        CatHttpPoint httpPoint = sendHandler.getHttpPoint();
        System.out.println("req=" + httpPoint.getRequestBody());
        System.out.println("resp=" + httpPoint.getResponseBody());
    }

    @Test
    public void demo3() throws Exception {
        Demo demo = creart();
        CatSendProcessor sendHandler = new CatSendProcessor();

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


    /**
     * 自动拆包异常
     * */
    @Test
    public void demo5() throws Exception {
        Demo resp1 = remote.demo5(System.currentTimeMillis());
        System.out.println(JSONObject.toJSONString(resp1));

        Demo resp2 = remote.demo5(System.currentTimeMillis());
        System.out.println(JSONObject.toJSONString(resp2));

    }

    /**
     * 自动拆包并检验异常
     * */
    @Test
    public void demo6() throws Exception {
        CatSendProcessor sendHandler = new CatSendProcessor();
        Void nul = remote.demo6(System.currentTimeMillis(), sendHandler, "bug猫");
    }

    @Test
    public void demo7() throws Exception {
        CatSendProcessor sendHandler = new CatSendProcessor();
        Demo resp = remote.demo7("bug猫");
        System.out.println(JSONObject.toJSONString(resp));
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