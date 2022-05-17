package cc.bugcat.example.catserver.serverApi;


import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.handler.CatInterceptPoint;
import cc.bugcat.catserver.handler.CatServerContextHolder;
import cc.bugcat.catserver.spi.CatInterceptorGroup;
import cc.bugcat.catserver.spi.CatServerInterceptor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * 拦截器
 * */
@Component
public class UserInterceptorConfig extends CatServerConfiguration {

    private CatServerInterceptor globalInterceptor;
    private List<CatInterceptorGroup> groupList;


    @Override
    public void afterPropertiesSet() {

        super.afterPropertiesSet();

        this.globalInterceptor = new CatServerInterceptor() {
            @Override
            public Object postHandle(CatServerContextHolder contextHolder) throws Throwable {
                CatInterceptPoint interceptPoint = contextHolder.getInterceptPoint();
                System.out.println("全局拦截器 => " + interceptPoint.getRequest().getRequestURI());
                return contextHolder.proceedRequest();
            }
        };

        this.groupList = new ArrayList<>();
        CatInterceptorGroup group = new CatInterceptorGroup(){
            @Override
            public boolean matcher(CatInterceptPoint interceptPoint) {
                String uri = interceptPoint.getRequest().getRequestURI();
                return uri.contains("111");
            }

            @Override
            public Supplier<List<CatServerInterceptor>> getInterceptorFactory() {
                return () -> Arrays.asList(new CatServerInterceptor(){
                    @Override
                    public Object postHandle(CatServerContextHolder contextHolder) throws Throwable {
                        System.out.println("运行时拦截器");
                        return contextHolder.proceedRequest();
                    }
                });
            }
        };
        groupList.add(group);
    }

    

    @Override
    public CatServerInterceptor getGlobalInterceptor() {
        return this.globalInterceptor;
    }

    @Override
    public List<CatInterceptorGroup> getInterceptorGroup() {
        return this.groupList;
    }
    
}
