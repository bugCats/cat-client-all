package cc.bugcat.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class CatServerApplication {

	public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CatServerApplication.class);
        app.run(args);

        System.out.println("http://localhost:8112/swagger-ui.html");
    }

}
