package cc.bugcat.example;

import cc.bugcat.catclient.annotation.EnableCatClient;
import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.example.catclient.serverApi.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@ComponentScan("cc.bugcat")
@EnableCatClient(value = "cc.bugcat",  classes = Config.class)    //开启被CatClient，并且扫描指定包路径
@SpringBootApplication
public class CatClientApplication {

	public static void main(String[] args) {
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
}
