package cc.bugcat.example;

import cc.bugcat.catserver.annotation.EnableCatServer;
import cc.bugcat.example.catserver.serverApi.UserInterceptorConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cglib.core.DebuggingClassWriter;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;

@EnableCatServer(configuration = UserInterceptorConfig.class)
@SpringBootApplication(scanBasePackages = "cc.bugcat")
public class CatServerApplication {

	public static void main(String[] args) {

	    //输出cglib动态代理字节码
//        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "E:\\tmp\\catface");

        SpringApplication app = new SpringApplication(CatServerApplication.class);
        app.run(args);
        System.out.println("http://localhost:8012/swagger-ui.html");
	}


    @Controller
    public static class IndexController{
        @RequestMapping("/")
        public ModelAndView index(){
            return new ModelAndView("redirect:swagger-ui.html");
        }
    }
    
    
    
}
