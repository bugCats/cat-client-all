package cc.bugcat.example.catclient.sign;

import cc.bugcat.catclient.handler.CatClientDepend;
import cc.bugcat.catclient.utils.CatClientBuilders;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import cc.bugcat.example.dto.DemoDTO;
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
        /**
         * 静态方法调用
         * 如果使用Spring容器启动，则不需要这些
         * */
        ((Logger) LoggerFactory.getLogger("ROOT")).setLevel(Level.ERROR);

        Properties prop = new Properties();
        prop.put("core-server.remoteApi", "http://127.0.0.1:8012");
        prop.put("demo.apikey", "签名示例密钥");

        CatClientDepend clientDepend = CatClientDepend.builder()
                .clientFactory(new SignFactory())
                .build();

        remote = CatClientBuilders.builder(SignRemote.class)
                    .clientDepend(clientDepend)
                    .environment(prop)
                    .build();
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
        demo.setUserkey("这是密钥12");
        remote.demo12(demo);// 对比控制台打印的签名sign字段
        
        demo.setUserkey("这是密钥13");
        remote.demo13(demo);// 对比控制台打印的签名sign字段
    }
}