package com.bugcat.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
public class CatServerApplication {

	public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CatServerApplication.class);
        app.run(args);

        System.out.println("http://localhost:8112/swagger-ui.html");
    }

}
