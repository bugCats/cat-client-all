package com.bugcat.example;

import com.bugcat.catserver.annotation.EnableCatServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cglib.core.DebuggingClassWriter;

@EnableCatServer("com.bugcat")
@SpringBootApplication
public class CatServerApplication {

	public static void main(String[] args) {
	    //动态生成的class存放目录，debug使用
//        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "f:\\tmp");
        SpringApplication app = new SpringApplication(CatServerApplication.class);
        app.run(args);
        System.out.println("http://localhost:8012/swagger-ui.html");
	}
    

}
