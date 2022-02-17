package cc.bugcat.catserver.config;

import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catserver.spi.CatInterceptor;
import cc.bugcat.catserver.spi.CatInterceptorGroup;
import cc.bugcat.catserver.spi.CatServerInterceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 全局默认值
 *
 *
 * @author bugcat
 * */
public class CatServerConfiguration {


    public static final Class wrapper = AbstractResponesWrapper.Default.class;


    /**
     * 统一的响应实体包装器类
     * */
    public Class<? extends AbstractResponesWrapper> wrapper(){
        return wrapper;
    }


    /**
     * 所有CatServer类共享的拦截器
     * 一般用于记录日志
     * */
    public Supplier<CatInterceptor> globalInterceptor(){
        return () -> null;
    }


    /**
     * 其他拦截器组
     * */
    public Supplier<List<CatInterceptorGroup>> interceptorGroup(){
        List<CatInterceptorGroup> groups = new ArrayList<>(0);
        return () -> groups;
    }



}
