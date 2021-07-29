package cc.bugcat.example;

import cc.bugcat.example.scanner.CatScannerRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;


@Import(CatScannerRegistrar.class)
@SpringBootApplication
public class CatServerApplication {

	public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CatServerApplication.class);
        app.run(args);

        System.out.println("http://localhost:8112/swagger-ui.html");
    }

}
