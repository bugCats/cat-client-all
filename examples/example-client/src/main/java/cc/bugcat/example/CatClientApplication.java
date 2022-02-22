package cc.bugcat.example;

import cc.bugcat.catclient.annotation.EnableCatClient;
import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.example.catclient.remote.ApiRemoteService1;
import cc.bugcat.example.catclient.remote.ApiRemoteService2;
import cc.bugcat.example.catclient.remote.ApiRemoteService3;
import cc.bugcat.example.catclient.remote.ApiRemoteService4;
import cc.bugcat.example.catclient.serverApi.Config;
import cc.bugcat.example.catclient.sign.SignRemote;
import cc.bugcat.example.catclient.token.TokenRemote;
import cc.bugcat.example.dto.Demo;
import cc.bugcat.example.dto.DemoDTO;
import cc.bugcat.example.tools.PageInfo;
import cc.bugcat.example.tools.ResponseEntity;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@ComponentScan
@EnableCatClient(value = "cc.bugcat.example",  classes = {Config.class})    //开启被CatClient，并且扫描指定包路径
@SpringBootApplication
public class CatClientApplication {


	public static void main(String[] args) {

	    //输出cglib动态代理字节码
//        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "E:\\tmp");

        SpringApplication app = new SpringApplication(CatClientApplication.class);
        app.run(args);

        System.out.println("http://localhost:8010/swagger-ui.html");
        System.out.println("测试时，请同时启动 example-server");
	}



	@Controller
	public static class IndexController{
	    @RequestMapping("/")
        public ModelAndView index(){
            return new ModelAndView("redirect:swagger-ui.html");
        }
    }




    /**
     * 模拟通过客户端发起调用
     *
     * 也可以使用 ApiRemote1Test、ApiRemote2Test、ApiRemote3Test 等单元测试类
     *
     * */
    @Api(tags = "客户端 - 一般场景")
    @RestController
    public static class NomalController {

        @Autowired
        private ApiRemoteService1 catRemoteApi1;
        @Autowired
        private ApiRemoteService2 catRemoteApi2;
        @Autowired
        private ApiRemoteService3 catRemoteApi3;
        @Autowired
        private ApiRemoteService4 catRemoteApi4;
        @Autowired
        private SignRemote signRemote;
        @Autowired
        private TokenRemote tokenRemote;

        @GetMapping("/demo1/cat1")
        public String cat1(){
            Demo demo = creart();
            demo.setId(123L);
            ResponseEntity<Demo> resp = catRemoteApi1.demo1(demo);
            return JSONObject.toJSONString(resp);
        }


        @GetMapping("/demo1/cat2")
        public String cat2(){
            Demo demo = creart();
            CatSendProcessor sendHandler = new CatSendProcessor();
            String resp = catRemoteApi2.demo2(sendHandler, demo);
            System.out.println("resp=" + resp);
            StringBuilder sbr = new StringBuilder();
            sbr.append("resp=").append(JSONObject.toJSONString(resp)).append("<br/>");
            sbr.append("req=").append(sendHandler.getHttpPoint().getResponseBody()).append("<br/>"); //此次http调用的入参、响应都砸sendHandler中
            return sbr.toString();
        }


        @GetMapping("/demo1/cat3")
        public String cat3(){

            Demo demo = creart();
            StringBuilder sbr = new StringBuilder();

            CatSendProcessor sendHandler = new CatSendProcessor();

            PageInfo<Demo> resp = catRemoteApi3.demo3(demo, sendHandler);
            sbr.append("第一次=").append(JSONObject.toJSONString(resp)).append("<br/>");

            demo.setId(3L);
            resp = catRemoteApi3.demo3(demo, sendHandler);
            sbr.append("第二次=").append(JSONObject.toJSONString(resp)).append("<br/>");

            return sbr.toString();
        }

        @GetMapping("/demo1/cat4")
        public ResponseEntity<Demo> cat4(){
            ResponseEntity<Demo> resp = catRemoteApi4.demo4("bug猫", "bug猫");
            return resp;
        }

        @GetMapping("/demo1/cat5")
        public ResponseEntity<Void> cat5(){
            DemoDTO demo = new DemoDTO();
            demo.setName("bugcat");
            demo.setMark("猫脸");
            demo.setUserkey("这是密钥");
            signRemote.demo12(demo);
            return ResponseEntity.ok(null);
        }


        @GetMapping("/demo1/cat6")
        public ResponseEntity<Void> cat6(){
            Demo demo = creart();
            ResponseEntity<String> token = tokenRemote.token1(demo);
            System.out.println("remote.token1=" + token.getData());
            return ResponseEntity.ok(null);
        }


        @GetMapping("/demo1/cat9")
        public ResponseEntity<String> cat9(){
            ResponseEntity<String> resp = catRemoteApi1.demo9();
            return resp;
        }



        private Demo creart(){
            Demo demo = new Demo();
            demo.setId(System.currentTimeMillis());
            demo.setName("bug猫");
            demo.setMark("调用");
            return demo;
        }
    }
}
