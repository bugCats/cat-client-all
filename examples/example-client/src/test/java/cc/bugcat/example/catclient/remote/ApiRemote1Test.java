package cc.bugcat.example.catclient.remote;

import cc.bugcat.catclient.handler.CatClientDepend;
import cc.bugcat.catclient.handler.CatHttpPoint;
import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.catclient.utils.CatClientBuilders;
import cc.bugcat.example.dto.Demo;
import cc.bugcat.example.dto.DemoEntity;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.alibaba.fastjson.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;


/**
 *
 * */
public class ApiRemote1Test {

    private static ApiRemoteService1 remote;


    @BeforeClass
    public static void beforeClass(){
        /**
         * 静态方法调用
         * 如果使用Spring容器启动，则不需要这些
         * */
        
        ((Logger) LoggerFactory.getLogger("ROOT")).setLevel(Level.ERROR);
        Properties prop = new Properties();
        prop.put("core-server.remoteApi", "http://127.0.0.1:8012");

        CatClientDepend depend = CatClientDepend.builder().environment(prop).build();
        remote = CatClientBuilders.builder(ApiRemoteService1.class)
                .clientDepend(depend)
                .build();
    }


    @Test
    public void demo1(){
        Demo demo = creart();
        ResponseEntity<Demo> resp = remote.demo1(demo);
        assertThat(resp).hasFieldOrPropertyWithValue("errCode", "10000"); 
        System.out.println("demo1=>" + JSONObject.toJSONString(resp));

    }

    @Test
    public void demo2(){
        Demo demo = creart();
        CatSendProcessor sendHandler = new CatSendProcessor();
        String resp = remote.demo2(sendHandler, new DemoEntity(demo));
        System.out.println("resp=>" + resp);

        CatHttpPoint httpPoint = sendHandler.getHttpPoint();
        System.out.println("req=>" + httpPoint.getRequestBody());
        System.out.println("resp=>" + httpPoint.getResponseBody());
    }

    @Test
    public void demo3(){
        Demo demo = creart();
        CatSendProcessor sendHandler = new CatSendProcessor();

        ResponseEntity<PageInfo<Demo>> resp = remote.demo3(demo, sendHandler);
        System.out.println("第一次=>" + JSONObject.toJSONString(resp));

        demo.setId(3L);
        demo.setName("不能有特殊字符");
        resp = remote.demo3(demo, sendHandler);
        System.out.println("第二次=>" + JSONObject.toJSONString(resp));
    }


    @Test
    public void demo4(){
        ResponseEntity<Demo> resp = remote.demo4("bug猫", "bug猫");
        System.out.println("resp1=>" + JSONObject.toJSONString(resp));

        ResponseEntity<Demo> resp2 = remote.demo4("bug猫2", "bug猫2");
        System.out.println("resp2=>" + JSONObject.toJSONString(resp2));
    }

    @Test
    public void demo5(){
        Demo resp1 = remote.demo5(System.currentTimeMillis());
        System.out.println("resp1=>" + JSONObject.toJSONString(resp1));

        Demo resp2 = remote.demo5(System.currentTimeMillis());
        System.out.println("resp2=>" + JSONObject.toJSONString(resp2));

    }

    @Test
    public void demo6(){
        CatSendProcessor sendHandler = new CatSendProcessor();
        Void nul = remote.demo6(System.currentTimeMillis(), sendHandler, "bug猫");
    }


    /**
     * 模拟调用服务端发生异常，返回默认的数据。
     * @see ApiRemoteService1#demo9()
     * */
    @Test
    public void demo9(){
        ResponseEntity<String> resp = remote.demo9();
        assertThat(resp).hasFieldOrPropertyWithValue("errMsg", "模拟发生http异常，返回默认数据");
//        System.out.println("resp=>" + JSONObject.toJSONString(resp));
    }


    private Demo creart(){
        Demo demo = new Demo();
        demo.setId(System.currentTimeMillis());
        demo.setName("bug猫");
        demo.setMark("调用");
        return demo;
    }

}