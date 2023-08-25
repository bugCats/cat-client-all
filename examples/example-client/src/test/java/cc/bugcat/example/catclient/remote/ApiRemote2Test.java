package cc.bugcat.example.catclient.remote;

import cc.bugcat.catclient.handler.CatClientDepend;
import cc.bugcat.catclient.handler.CatClientMockProvideBuilder;
import cc.bugcat.catclient.handler.CatHttpPoint;
import cc.bugcat.catclient.spi.CatClientMockProvide;
import cc.bugcat.catclient.utils.CatClientBuilders;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.alibaba.fastjson.JSONObject;
import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.example.dto.Demo;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.*;
import java.util.Properties;
import java.util.Set;


/**
 * 异常增强版。
 * ApiRemoteService2配置了异常回调类：fallback=ApiRemoteService2Fallback.class；
 * 当发生http异常时，会执行回调类对应的方法，返回默认数据
 * */
public class ApiRemote2Test {

    private static ApiRemoteService2 remote;
    
    
    @BeforeClass
    public static void beforeClass(){
        /**
         * 静态方法调用
         * 如果使用Spring容器启动，则不需要这些
         * */
        ((Logger) LoggerFactory.getLogger("ROOT")).setLevel(Level.ERROR);

        Properties prop = new Properties();
        prop.put("core-server.remoteApi", "http://127.0.0.1:8012");

        CatClientMockProvideBuilder mockProvideBuilder = CatClientMockProvide.builder();
        mockProvideBuilder.mockClient(ApiRemoteService2Mock.class);
        
        CatClientDepend depend = CatClientDepend.builder()
                .environment(prop)
                .mockProvide(mockProvideBuilder.build())  //是否启用mock
                .build();
        
        remote = CatClientBuilders.builder(ApiRemoteService2.class)
                .clientDepend(depend)
                .build();
    }
    
    public static class ApiRemoteService2Mock implements ApiRemoteService2 {
        @Override
        public ResponseEntity<Demo> demo1(Demo req) {
            Demo demo = new Demo();
            demo.setMark("ApiRemoteService2Mock.demo1");
            return ResponseEntity.ok(demo);
        }

        @Override
        public String demo2(CatSendProcessor send, Demo req) {
            return "ApiRemoteService2Mock.demo2";
        }

        @Override
        public ResponseEntity<String> demo9(PageInfo<Demo> pageInfo) {
            return ResponseEntity.ok("ApiRemoteService2Mock.demo9");
        }
    
    }


    /**
     * 发生异常回调
     * */
    @Test
    public void demo1() {
        Demo demo = creart();
        ResponseEntity<Demo> resp = remote.demo1(demo);
//        assertThat(resp).hasFieldOrPropertyWithValue("errMsg", "demo1实际调用失败, 这是ApiRemote2Error异常回调类返回的");
        System.err.println(JSONObject.toJSONString(resp));

    }

    @Test
    public void demo2() {
        Demo demo = creart();
        CatSendProcessor sendHandler = new CatSendProcessor();
        String resp = remote.demo2(sendHandler, demo);
        System.err.println("resp=" + resp);

        CatHttpPoint httpPoint = sendHandler.getHttpPoint();
        System.err.println("req=" + httpPoint.getRequestBody());
        System.err.println("resp=" + httpPoint.getResponseBody());
    }



    @Test
    public void demo9() {
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