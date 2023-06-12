package cc.bugcat.example.catclient.remote;

import cc.bugcat.catclient.handler.CatClientDepend;
import cc.bugcat.catclient.handler.CatHttpPoint;
import cc.bugcat.catclient.utils.CatClientBuilders;
import cc.bugcat.example.dto.DemoEntity;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.alibaba.fastjson.JSONObject;
import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.example.dto.Demo;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

/**
 * 去响应包装器类版
 *
 * 服务端代码 cc.bugcat.example.catclient.ApiRemoteController
 *
 * 
 * 注意服务端controller的返回数据类型： demo1~demo4均为ResponseEntity&lt;T&gt;, 但是在本示例中, 直接使用泛型对象接收；
 *
 * */
public class ApiRemote3Test {


    private static ApiRemoteService3 remote;
    
    
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
        remote = CatClientBuilders.builder(ApiRemoteService3.class)
                .clientDepend(depend)
                .build();
    }


    @Test
    public void demo1() {
        Demo query = creart();

        //controller响应ResponseEntity<Demo>, 本示例中直接使用Demo接收, 客户端自动拆包
        Demo resp = remote.demo1(query);
        System.out.println(JSONObject.toJSONString(resp));
    }


    @Test
    public void demo2() {
        Demo query = creart();
        CatSendProcessor sendHandler = new CatSendProcessor();

        //controller响应ResponseEntity<Demo>, 示例中使用Object接收, 表示不需要反序列化。
        Object resp = remote.demo2(sendHandler, new DemoEntity(query));
        assertThat(resp).isExactlyInstanceOf(String.class).as("响应使用Object类型表示不转换");
        System.out.println("resp=" + resp);

        CatHttpPoint httpPoint = sendHandler.getHttpPoint();
        System.out.println("req=" + httpPoint.getRequestBody());
        System.out.println("resp=" + httpPoint.getResponseBody());
    }


    @Test
    public void demo3() {
        Demo query = creart();
        CatSendProcessor sendHandler = new CatSendProcessor();

        //controller响应ResponseEntity<PageInfo<Demo>>, 示例使用PageInfo<Demo>接收, 自动拆包
        PageInfo<Demo> resp = remote.demo3(query, sendHandler);
        System.out.println("第一次=" + JSONObject.toJSONString(resp));

        //controller响应ResponseEntity<PageInfo<Demo>>，示例使用ResponseEntity<PageInfo<Demo>>接收, 直接返回
        ResponseEntity<PageInfo<Demo>> reps2 = remote.demo32(query, sendHandler);
        System.out.println("第二次=" + JSONObject.toJSONString(reps2));
    }


    @Test
    public void demo4() {

        //controller响应ResponseEntity<Demo>, 示例直接使用ResponseEntity<Demo>接收, 不自动拆包
        ResponseEntity<Demo> resp = remote.demo4("bug猫", "bug猫");
        System.out.println(JSONObject.toJSONString(resp));

    }
    
    
    @Test
    public void demo5() {
        /**
         * 服务端返回Demo，但是interface上添加了@CatResponesWrapper，表示需要拆包。
         * 客户端会推断服务端应该返回ResponseEntity<T>类型，但是服务端实际返回Demo。会造成反序列化成ResponseEntity后，属性对应不上。
         * 客户端拆包后，会自动检查errCode {@link ResponseEntityWrapper#checkValid(cc.bugcat.example.tools.ResponseEntity)}
         * 此处会抛出异常
         * */
        try {
            Demo resp1 = remote.demo5(System.currentTimeMillis());
            System.out.println(JSONObject.toJSONString(resp1));
        } catch ( Exception e ) {
            assertThat(e).hasMessageContaining("null");
        }
    }

    @Test
    public void demo6() {

        //controller响应ResponseEntity<Void>, 示例使用Void|void接收, 表示无业务参数
        Void nul = remote.demo6("demo6", "bug猫");
        assertThat(nul).isExactlyInstanceOf(Void.class);

        /**
         * controller响应ResponseEntity<Void>, 示例使用Void接收, 表示无业务参数
         * 抛出自定义异常
         * */
        try {
            CatSendProcessor sendHandler = new CatSendProcessor();
            remote.demo7(45658L, sendHandler, "bug猫");
        } catch ( Exception e ) {
            assertThat(e).hasMessageContaining("自定义异常code");
        }
    }


    @Test
    public void demo9() {
        try {
            /**
             * 模拟发生http异常
             * */
            String resp = remote.demo9();
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