package cc.bugcat.example.catclient.sign;

import cc.bugcat.catclient.handler.CatClientDepend;
import cc.bugcat.catclient.utils.CatClientBuilders;
import cc.bugcat.example.CatClientApplication;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import cc.bugcat.example.dto.DemoDTO;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * 需要签名的呆毛；
 * 
 * SignRemote 使用自定义factory=SignFactory.class调整流程
 * */
public class SignRemoteTest {

    private static SignRemote remote;

    @BeforeClass
    public static void beforeClass(){
        /**
         * 静态方法调用
         * 如果使用Spring容器启动，则不需要这些
         * */
        ((Logger) LoggerFactory.getLogger("ROOT")).setLevel(Level.ERROR);

        Properties prop = new Properties();
        prop.put("core-server.remoteApi", "http://127.0.0.1:8012");
        prop.put("demo.apikey", "签名示例密钥");
        prop.put("tools", new Object() {
            public String out(String spring, String spEL){ //填充springEL
                System.out.println("tools.spring=" + spring);
                System.out.println("tools.spEL=" + spEL);
                return "tools.out";
            }
        });
        
        CatClientDepend clientDepend = CatClientDepend.builder()
                .clientFactory(new SignFactory())
                .environment(prop)
                .build();

        remote = CatClientBuilders.builder(SignRemote.class)
                    .clientDepend(clientDepend)
                    .build();
    }


    @Test
    public void demo11() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "bugcat");
        map.put("project", "");
        map.put("mark", "签名");
        remote.demo11(map);// 对比控制台打印的签名sign字段
    }


    @Test
    public void demo12() {
        DemoDTO demo = new DemoDTO();
        demo.setName("bugcat");
        demo.setMark("猫脸");
        demo.setUserkey("这是密钥12");
        remote.demo12(demo);// 对比控制台打印的签名sign字段

        System.out.println();
        
        demo.setUserkey("这是密钥13");
        remote.demo13(demo);// 对比控制台打印的签名sign字段
    }
}