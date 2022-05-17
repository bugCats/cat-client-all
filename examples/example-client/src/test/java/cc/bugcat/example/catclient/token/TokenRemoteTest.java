package cc.bugcat.example.catclient.token;

import cc.bugcat.catclient.beanInfos.CatClients;
import cc.bugcat.catclient.handler.CatClientDepend;
import cc.bugcat.catclient.utils.CatClientBuilders;
import cc.bugcat.example.dto.Demo;
import cc.bugcat.example.tools.ResponseEntity;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * 单元测试类
 * */
public class TokenRemoteTest {

    private static TokenRemote remote;
    static {
        ((Logger) LoggerFactory.getLogger("ROOT")).setLevel(Level.ERROR);
        /**
         * 静态方法调用
         * 如果使用Spring容器启动，则不需要这些
         * 用来填充客户端中使用的${xxx}
         * */
        Properties prop = new Properties();
        prop.put("core-server.remoteApi", "");
        prop.put("demo.username", "bugcat");
        prop.put("demo.pwd", "[密码]");

        CatClientDepend clientDepend = CatClientDepend.builder()
                .defaultSendInterceptor(new TokenInterceptor())
                .build();
        remote = CatClientBuilders.builder(TokenRemote.class)
                .clientDepend(clientDepend)
                .catClient(new CatClients(){
                    @Override
                    public String host() { // 在运行时，动态修改
                        return "http://127.0.0.1:8012";
                    }
                })
                .environment(prop)
                .build();
    }
    
    @Test
    public void token() {
        Demo demo = new Demo();
        demo.setName("bugcat");
        demo.setMark("猫脸");
        ResponseEntity<String> sendDemo1 = remote.sendDemo1(demo);
        System.out.println("remote.sendDemo1=" + sendDemo1.getData());

        String token = "TokenRemoteTest-token2";
        ResponseEntity<String> sendDemo2 = remote.sendDemo2(demo, token);
        System.out.println("remote.sendDemo2=" + sendDemo2.getData());

        String url = "/cat/token";
        ResponseEntity<String> sendDemo3 = remote.sendDemo3(url, "TokenRemoteTest-token3", JSONObject.toJSONString(demo));
        System.out.println("remote.sendDemo3=" + sendDemo3.getData());

    }

}