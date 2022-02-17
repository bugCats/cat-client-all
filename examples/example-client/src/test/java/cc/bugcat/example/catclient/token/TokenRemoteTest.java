package cc.bugcat.example.catclient.token;

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

public class TokenRemoteTest {

    private static TokenRemote remote;
    static {
        /**
         * 静态方法调用
         * 如果使用Spring容器启动，则不需要这些
         * */
        ((Logger) LoggerFactory.getLogger("ROOT")).setLevel(Level.ERROR);

        Properties prop = new Properties();
        prop.put("core-server.remoteApi", "http://127.0.0.1:8012");
        prop.put("demo.username", "bugcat");
        prop.put("demo.pwd", "[密码]");

        CatClientDepend clientDepend = CatClientDepend.builder()
                .defaultSendInterceptor(new TokenInterceptor())
                .build();

        remote = CatClientBuilders.builder(TokenRemote.class)
                .clientDepend(clientDepend)
                .environment(prop)
                .build();
    }



    @Test
    public void token() throws Exception {
        Demo demo = new Demo();
        demo.setName("bugcat");
        demo.setMark("猫脸");
        ResponseEntity<String> token1 = remote.token1(demo);
        System.out.println("remote.token1=" + token1.getData());

        String token = "TokenRemoteTest-token2";
        ResponseEntity<String> token2 = remote.token2(demo, token);
        System.out.println("remote.token2=" + token2.getData());

        String url = "/cat/token";
        ResponseEntity<String> token3 = remote.token3(url, "TokenRemoteTest-token3", JSONObject.toJSONString(demo));
        System.out.println("remote.token3=" + token3.getData());

    }

}