package cc.bugcat.example.catclient.remote;

import cc.bugcat.catclient.handler.CatClientDepend;
import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.handler.CatJacksonResolver;
import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.catclient.handler.CatHttpPoint;
import cc.bugcat.catclient.spi.CatPayloadResolver;
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
 * 客户端继承
 * */
public class ApiRemote4Test {


    /**
     * 是子类
     * */
    private static ApiRemoteService4Plus remote;
    
    
    @BeforeClass
    public static void beforeClass(){
        /**
         * 静态方法调用
         * 如果使用Spring容器启动，则不需要这些
         * */
        ((Logger) LoggerFactory.getLogger("ROOT")).setLevel(Level.ERROR);

        Properties prop = new Properties();
        prop.put("core-server.remoteApi", "http://127.0.0.1:8012");

        CatClientConfiguration configuration = new CatClientConfiguration(){
            @Override
            public int getSocket() {
                return 30000;
            }

            @Override
            public int getConnect() {
                return 30000;
            }

            @Override
            public CatPayloadResolver getPayloadResolver() {
                return new CatJacksonResolver();
            }
        };
        configuration.afterPropertiesSet();

        CatClientDepend clientDepend = CatClientDepend.builder()
                .clientConfig(configuration)
                .environment(prop)
                .build();
        remote = CatClientBuilders.builder(ApiRemoteService4Plus.class)
                .clientDepend(clientDepend)
                .build();
    }





    @Test
    public void demo1()  {
        Demo demo = creart();
        ResponseEntity<Demo> resp = remote.demo1(demo);
        System.out.println(JSONObject.toJSONString(resp));

    }

    @Test
    public void demo2()  {
        Demo demo = creart();
        CatSendProcessor sendHandler = new CatSendProcessor();
        Object resp = remote.demo2(sendHandler, new DemoEntity(demo));
        System.out.println("resp=" + (String)resp);

        CatHttpPoint httpPoint = sendHandler.getHttpPoint();
        System.out.println("req=" + httpPoint.getRequestBody());
        System.out.println("resp=" + httpPoint.getResponseBody());
    }

    @Test
    public void demo3()  {
        Demo demo = creart();
        CatSendProcessor sendHandler = new CatSendProcessor();

        ResponseEntity<PageInfo<Demo>> resp = remote.demo3(demo, sendHandler);
        System.out.println("第一次=" + JSONObject.toJSONString(resp));

        demo.setId(3L);
        resp = remote.demo3(demo, sendHandler);
        System.out.println("第二次=" + JSONObject.toJSONString(resp));
    }


    @Test
    public void demo4()  {
        ResponseEntity<Demo> resp = remote.demo4("bug猫", "bug猫");
        System.out.println(JSONObject.toJSONString(resp));
        ResponseEntity<Demo> resp2 = remote.demo4("bug猫2", "bug猫2");
        System.out.println(JSONObject.toJSONString(resp2));
    }


    /**
     * 自动拆包异常
     * @see cc.bugcat.example.catclient.remote.ApiRemote3Test#demo5()
     * */
    @Test
    public void demo5()  {
        try {
            Demo resp1 = remote.demo5(System.currentTimeMillis());
            System.out.println(JSONObject.toJSONString(resp1));
        } catch ( Exception e ) {
            assertThat(e).hasMessageContaining("null");
        }

    }

    /**
     * 自动拆包并检验异常
     * @see cc.bugcat.example.catclient.remote.ApiRemote3Test#demo6()
     * */
    @Test
    public void demo6()  {
        try {
            CatSendProcessor sendHandler = new CatSendProcessor();
            Void nul = remote.demo6(System.currentTimeMillis(), sendHandler, "bug猫");
        } catch ( Exception e ) {
            assertThat(e).hasMessageContaining("自定义异常说明");
        }
    }

    @Test
    public void demo7()  {
        CatSendProcessor sendHandler = new CatSendProcessor();
        Demo resp = remote.demo7("bug猫");
        System.out.println(JSONObject.toJSONString(resp));
    }

    @Test
    public void demo9()  {
        try {
            /**
             * 模拟发生http异常
             * */
            ResponseEntity<String> resp = remote.demo9();
            System.out.println(JSONObject.toJSONString(resp));
        } catch ( Exception e ) {
            assertThat(e).hasMessageContaining("404");
        }
    }


    private Demo creart(){
        Demo demo = new Demo();
        demo.setId(System.currentTimeMillis());
        demo.setName("bug猫");
        demo.setMark("调用");
        return demo;
    }

}