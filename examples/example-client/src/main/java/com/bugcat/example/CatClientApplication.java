package com.bugcat.example;

import com.bugcat.catclient.annotation.EnableCatClient;
import com.bugcat.example.catclient.remote.ApiRemote1;
import com.bugcat.example.catclient.serverApi.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cglib.core.DebuggingClassWriter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@ComponentScan("com.bugcat")
@EnableCatClient(value = "com.bugcat",  classes = Config.class)    //开启被CatClient，并且扫描指定包路径
@SpringBootApplication
public class CatClientApplication {

	public static void main(String[] args) {	
	    
	    //动态生成的class存放目录，debug使用
//        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "f:\\tmp");
        
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
