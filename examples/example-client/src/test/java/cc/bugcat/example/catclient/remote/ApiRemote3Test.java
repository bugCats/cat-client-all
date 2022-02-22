package cc.bugcat.example.catclient.remote;

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
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Properties;


/**
 * 去响应包装器类版
 * */
public class ApiRemote3Test {


    private static ApiRemoteService3 remote;
    static {
        /**
         * 静态方法调用
         * 如果使用Spring容器启动，则不需要这些
         * */
        ((Logger) LoggerFactory.getLogger("ROOT")).setLevel(Level.ERROR);

        Properties prop = new Properties();
        prop.put("core-server.remoteApi", "http://127.0.0.1:8012");

        remote = CatClientBuilders.builder(ApiRemoteService3.class)
                .environment(prop)
                .build();
    }


    /**
     *
     * 服务端代码 {@link cc.bugcat.example.catclient.ApiRemoteController}
     *
     * 注意controller返回对象, demo1~demo4均为ResponseEntity, 但是在本示例中, 直接使用泛型对象接收
     *
     * */
    @Test
    public void demo1() throws Exception {
        Demo query = creart();

        // controller响应ResponseEntity<Demo>, 示例使用Demo接收, 自动拆包
        Demo resp = remote.demo1(query);
        System.out.println(JSONObject.toJSONString(resp));
    }


    @Test
    public void demo2() throws Exception {
        Demo query = creart();
        CatSendProcessor sendHandler = new CatSendProcessor();

        //controller响应ResponseEntity<Demo>, 示例中使用String接收, 表示不需要反序列化
        String resp = remote.demo2(sendHandler, new DemoEntity(query));
        System.out.println("resp=" + resp);

        CatHttpPoint httpPoint = sendHandler.getHttpPoint();
        System.out.println("req=" + httpPoint.getRequestBody());
        System.out.println("resp=" + httpPoint.getResponseBody());
    }


    @Test
    public void demo3() throws Exception {
        Demo query = creart();
        CatSendProcessor sendHandler = new CatSendProcessor();

        //controller响应ResponseEntity<PageInfo<Demo>>, 示例使用PageInfo<Demo>接收, 自动拆包
        PageInfo<Demo> resp = remote.demo3(query, sendHandler);
        System.out.println("第一次=" + JSONObject.toJSONString(resp));

        query.setId(3L);
        resp = remote.demo3(query, sendHandler);
        System.out.println("第二次=" + JSONObject.toJSONString(resp));
    }


    @Test
    public void demo4() throws Exception {

        //controller响应ResponseEntity<Demo>, 示例直接使用ResponseEntity<Demo>接收, 不自动拆包
        ResponseEntity<Demo> resp = remote.demo4("bug猫", "bug猫");
        System.out.println(JSONObject.toJSONString(resp));


        ResponseEntity<Demo> resp2 = remote.demo4("bug猫2", "bug猫2");
        System.out.println(JSONObject.toJSONString(resp2));
    }

    /**
     *
     * */
    @Test
    public void demo5() throws Exception {
        /**
         * 服务端返回Demo，但是interface上添加了@CatResponesWrapper，表示需要拆包。
         * 客户端拆包后，会自动检查errCode {@link ResponseEntityWrapper#checkValid(cc.bugcat.example.tools.ResponseEntity)}
         * 此处会抛出异常
         * */
        Demo resp1 = remote.demo5(System.currentTimeMillis());
        System.out.println(JSONObject.toJSONString(resp1));

        Demo resp2 = remote.demo5(System.currentTimeMillis());
        System.out.println(JSONObject.toJSONString(resp2));

    }

    @Test
    public void demo6() throws Exception {
        CatSendProcessor sendHandler = new CatSendProcessor();

        /**
         * controller响应ResponseEntity<Void>, 示例使用Void接收, 表示无业务参数
         * 抛出自定义异常
         * */
        Void nul = remote.demo6(System.currentTimeMillis(), sendHandler, "bug猫");
    }


    @Test
    public void demo9() throws Exception {
        /**
         * 模拟发生http异常
         * */
        String resp = remote.demo9();
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