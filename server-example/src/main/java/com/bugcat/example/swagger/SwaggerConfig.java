package com.bugcat.example.swagger;

import com.bugcat.catserver.annotation.CatServer;
import io.swagger.annotations.Api;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@ComponentScan("springfox.documentation")
public class SwaggerConfig {
    
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("bugcat").description("bugcat")
                .termsOfServiceUrl("").version("1.0").build();
    }
    @Bean
    public Docket createApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                
//                /**
//                 * 如果把 @Api 放在interface上，并且需要兼容RequestHandlerSelectors.withClassAnnotation(Api.class)
//                 * apis方法需要改成这样
//                 * */
//                .apis(input -> {
//                    Class<?> beanType = input.getHandlerMethod().getBeanType();
//                    return beanType.isAnnotationPresent(Api.class) || beanType.isAnnotationPresent(CatServer.class);
//                })
                
                .paths(PathSelectors.any())
                .build();
    }
    
    
    
}