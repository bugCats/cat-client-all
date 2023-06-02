package cc.bugcat.catserver.handler;

import cc.bugcat.catserver.asm.CatVirtualParameterEnhancer.CatVirtualParameter;
import cc.bugcat.catserver.spi.CatParameterResolver;
import cc.bugcat.catserver.spi.CatServerInterceptor;

/**
 * 默认对象与默认类
 * @author bugcat
 * */
public class CatServerDefaults {


    /**
     * 默认拦截器
     * */
    public static final CatServerInterceptor DEFAULT_INTERCEPTOR = new CatServerInterceptor() {};
    
    /**
     * 默认方法入参预处理
     * */
    public static final CatParameterResolver DEFAULT_RESOLVER = new CatParameterResolver(){};
    /**
     * 精简模式方法入参预处理
     * */
    public static final CatParameterResolver FACE_RESOLVER = new FaceParameterResolver();





    /**
     * 精简模式下参数预处理。
     * 精简模式下，默认使用post+json请求，
     * 增强interface的方法有且仅有一个虚拟入参对象VirtualParameter，
     * 方取arguments第一个元素，调用VirtualParameter.toArray转换为实际参数列表。
     * */
    private static final class FaceParameterResolver implements CatParameterResolver {
        @Override
        public Object[] resolveArguments(CatMethodInfo methodInfo, Object[] arguments) throws Exception {
            if( arguments == null || arguments.length == 0 ){
                return arguments;
            }
            CatVirtualParameter requestBody = (CatVirtualParameter) arguments[0];
            Object[] args = requestBody.toArray();
            return args;
        }
    }


}
