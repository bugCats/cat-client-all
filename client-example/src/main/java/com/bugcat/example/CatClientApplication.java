package com.bugcat.example;

import com.bugcat.catclient.annotation.EnableCatClient;
import com.bugcat.example.catclient.remote.ApiRemote1;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@ComponentScan("com.bugcat")
//@EnableCatClient(classes = ApiRemote1.class)    //开启被CatClient，并且扫描指定包路径
@EnableCatClient("com.bugcat")    //开启被CatClient，并且扫描指定包路径
@SpringBootApplication
public class CatClientApplication {

	public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CatClientApplication.class);
        app.run(args);
	}

	
	@Controller
	public static class IndexController{
	    @RequestMapping("/")
        public ModelAndView index(){
            return new ModelAndView("redirect:swagger-ui.html");
        }
    }
}
