package com.bugcat.example;

import com.bugcat.catserver.annotation.EnableCatServer;
import com.bugcat.example.api.UserService;
import com.bugcat.example.api.vo.UserInfo;
import com.bugcat.example.dto.Demo;
import com.bugcat.example.tools.ResponseEntity;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cglib.core.DebuggingClassWriter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@EnableCatServer("com.bugcat")
@SpringBootApplication
public class CatServerApplication {

	public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CatServerApplication.class);
        app.run(args);
	}

	
	
	@Api
	@Controller
	public static class IndexController{

        /**
         * 也可以作为普通组件自动注入
         * */
        @Autowired
        private UserService userService;
	    
	    @GetMapping("/")
        public ModelAndView index(){
            return new ModelAndView("redirect:swagger-ui.html");
        }

        @GetMapping("/userInfo")
        public ResponseEntity<UserInfo> userInfo(){
            UserInfo userInfo = userService.userInfo("666");
            return ResponseEntity.ok(userInfo);
        }
    }
    

}
