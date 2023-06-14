package cc.bugcat.catserver.handler;


import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catserver.asm.CatVirtualParameterEnhancer;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.spi.CatParameterResolver;
import cc.bugcat.catserver.spi.CatResultHandler;
import cc.bugcat.catserver.spi.CatServerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


/**
 * 服务端前置依赖项
 * */
public class CatServerDepend {

    public static final String BEAN_NAME = "catServerDepend";
    
    
    @Autowired
    private CatServerConfiguration serverConfig;

    @Autowired(required = false)
    private List<AbstractResponesWrapper> wrappers;
    
    @Autowired(required = false)
    private List<CatResultHandler> resultHandlers;
    
    @Autowired(required = false)
    private List<CatServerInterceptor> serverInterceptors;

    @Autowired(required = false)
    private List<CatParameterResolver> parameterResolvers;



    /**
     * 默认拦截器
     * */
    public static final CatServerInterceptor DEFAULT_INTERCEPTOR = new CatServerInterceptor() {};

    
    /**
     * 默认方法入参预处理
     * */
    public static final CatParameterResolver DEFAULT_RESOLVER = new DefaultParameterResolver();
    
    /**
     * 精简模式方法入参预处理
     * */
    public static final CatParameterResolver FACE_RESOLVER = new FaceParameterResolver();


    
    
    /**
     * 默认的方法入参处理
     * */
    private static final class DefaultParameterResolver implements CatParameterResolver {
        @Override
        public Object[] resolveArguments(CatMethodInfo methodInfo, Object[] args) throws Exception {
            return args;
        }
    }

    
    /**
     * 精简模式下参数预处理。
     * 精简模式下，默认使用post+Json请求，
     * 增强interface的方法有且仅有一个虚拟入参对象VirtualParameter，
     * 方取arguments第一个元素，调用VirtualParameter.toArray转换为实际参数列表。
     * */
    private static final class FaceParameterResolver implements CatParameterResolver {
        @Override
        public Object[] resolveArguments(CatMethodInfo methodInfo, Object[] arguments) {
            if( arguments == null || arguments.length == 0 ){
                return arguments;
            }
            CatVirtualParameterEnhancer.CatVirtualParameter requestBody = (CatVirtualParameterEnhancer.CatVirtualParameter) arguments[0];
            Object[] args = requestBody.toArray();
            return args;
        }
    }


}
