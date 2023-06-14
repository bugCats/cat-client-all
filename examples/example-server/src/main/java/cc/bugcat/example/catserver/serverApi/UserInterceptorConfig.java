package cc.bugcat.example.catserver.serverApi;


import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.handler.CatInterceptPoint;
import cc.bugcat.catserver.handler.CatServerContextHolder;
import cc.bugcat.catserver.spi.CatInterceptorGroup;
import cc.bugcat.catserver.spi.CatServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * 拦截器
 * */
@Configuration
public class UserInterceptorConfig extends CatServerConfiguration {

    private CatServerInterceptor userGlobalInterceptor;


    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        this.userGlobalInterceptor = new CatServerInterceptor() {
            @Override
            public Object postHandle(CatServerContextHolder contextHolder) throws Throwable {
                CatInterceptPoint interceptPoint = contextHolder.getInterceptPoint();
                System.out.println("全局拦截器，可以被自定义拦截器覆盖。这是自定义全局拦截器 => " + interceptPoint.getRequest().getRequestURI());
                return contextHolder.proceedRequest();
            }
        };
    }

    

    @Override
    public CatServerInterceptor getServerInterceptor() {
        return this.userGlobalInterceptor;
    }


    
    
    /**
     * 另外一种方式定义
     * */
    @Bean
    public CatInterceptorGroup interceptorGroup(){
        return new CatInterceptorGroup() {
            /**
             * 匹配分组
             */
            @Override
            public boolean matcher(CatInterceptPoint interceptPoint) {
                return true; //匹配所有CatServer类
            }

            /**
             * 如果匹配上，则执行这些拦截器
             */
            @Override
            public Supplier<List<CatServerInterceptor>> getInterceptorFactory() {
                return () -> Arrays.asList(new CatServerInterceptor(){
                    /**
                     * 执行拦截器
                     */
                    @Override
                    public Object postHandle(CatServerContextHolder contextHolder) throws Throwable {
                        System.out.println("全局拦截器，不可以被覆盖，只能使用CatServerInterceptor.GroupOff.class关闭");
                        return contextHolder.proceedRequest();
                    }
                });
            }
        };
    }
    
    
}
