package cc.bugcat.example.catserver.serverApi;


import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.handler.CatInterceptPoint;
import cc.bugcat.catserver.handler.CatServerContextHolder;
import cc.bugcat.catserver.spi.CatInterceptor;
import cc.bugcat.catserver.spi.CatInterceptorGroup;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * 拦截器
 * */
@Component
public class UserInterceptorConfig extends CatServerConfiguration{

    @Override
    public Supplier<CatInterceptor> globalInterceptor() {
        CatInterceptor globalInterceptor = new CatInterceptor() {
            @Override
            public Object postHandle(CatServerContextHolder contextHolder) throws Exception {
                CatInterceptPoint interceptPoint = contextHolder.getInterceptPoint();
                System.out.println("全局拦截器 => " + interceptPoint.getRequest().getRequestURI());
                return contextHolder.executeRequest();
            }
        };
        return () -> globalInterceptor;
    }

    @Override
    public Supplier<List<CatInterceptorGroup>> interceptorGroup() {
        List<CatInterceptorGroup> groups = new ArrayList<>();

        CatInterceptorGroup group1 = new CatInterceptorGroup(){
            @Override
            public boolean matcher(CatInterceptPoint interceptPoint) {
                String uri = interceptPoint.getRequest().getRequestURI();
                return uri.contains("111");
            }

            @Override
            public List<CatInterceptor> getInterceptors() {
                return Arrays.asList(new CatInterceptor(){
                    @Override
                    public Object postHandle(CatServerContextHolder contextHolder) throws Exception {
                        System.out.println("运行时拦截器");
                        return contextHolder.executeRequest();
                    }
                });
            }
        };

        groups.add(group1);

        return () -> groups;
    }
}
