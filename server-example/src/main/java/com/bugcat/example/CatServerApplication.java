package com.bugcat.example;

import com.bugcat.catserver.annotation.EnableCatServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@EnableCatServer("com.bugcat")
@SpringBootApplication
public class CatServerApplication {

	public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CatServerApplication.class);
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
