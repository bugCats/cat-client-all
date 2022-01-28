package cc.bugcat.example.catclient.remote;

import cc.bugcat.catclient.spi.CatHttpPoint;
import cc.bugcat.catclient.utils.CatClientBuilders;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.alibaba.fastjson.JSONObject;
import cc.bugcat.catclient.handler.CatSendProcessor;
import cc.bugcat.catclient.utils.CatClientUtil;
import cc.bugcat.example.dto.Demo;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Properties;


/**
 * 异常增强版
 * */
public class ApiRemote2Test {

    private static ApiRemote2 remote;
    static {
        ((Logger) LoggerFactory.getLogger("ROOT")).setLevel(Level.ERROR);

        Properties prop = new Properties();
        prop.put("core-server.remoteApi", "http://127.0.0.1:8012");

        remote = CatClientBuilders.builder(ApiRemote2.class)
                .environment(prop)
                .build();
    }


    /**
     * 发生异常回调
     * */
    @Test
    public void demo1() throws Exception {
        Demo demo = creart();
        ResponseEntity<Demo> resp = remote.demo1(demo);
        System.err.println(JSONObject.toJSONString(resp));

    }

    @Test
    public void demo2() throws Exception {
        Demo demo = creart();
        CatSendProcessor sendHandler = new CatSendProcessor();
        String resp = remote.demo2(sendHandler, demo);

        System.err.println("resp=" + resp);

        CatHttpPoint httpPoint = sendHandler.getHttpPoint();
        System.err.println("req=" + httpPoint.getRequestBody());
        System.err.println("resp=" + httpPoint.getResponseBody());
    }



    @Test
    public void demo9() throws Exception {
        PageInfo<Demo> pageInfo = new PageInfo<>();
        ResponseEntity<String> resp = remote.demo9(pageInfo);
        System.err.println(JSONObject.toJSONString(resp));
    }


    private Demo creart(){
        Demo demo = new Demo();
        demo.setId(System.currentTimeMillis());
        demo.setName("bug猫");
        demo.setMark("调用");
        return demo;
    }

}