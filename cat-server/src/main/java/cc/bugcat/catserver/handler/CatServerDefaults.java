package cc.bugcat.catserver.handler;

import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catserver.spi.CatInterceptor;
import cc.bugcat.catserver.spi.CatParameterResolver;
import cc.bugcat.catserver.spi.CatServerInterceptor;
import cc.bugcat.catserver.spi.CatServerResultHandler;
import cc.bugcat.catserver.asm.CatVirtualParameterEnhancer.VirtualParameter;

/**
 * 默认对象与默认类
 * @author bugcat
 * */
public class CatServerDefaults {


    /**
     * 默认拦截器组
     * */
    public static final CatServerInterceptor DEFAULT_INTERCEPTOR = new CatServerInterceptor() {};
    /**
     * 关闭拦截器
     * */
    public static final CatServerInterceptor OFF_INTERCEPTOR = new CatInterceptor.Off();


    /**
     * 默认方法入参预处理
     * */
    public static final CatParameterResolver DEFAULT_RESOLVER = new CatParameterResolver(){};
    /**
     * 精简模式方法入参预处理
     * */
    public static final CatParameterResolver FACE_RESOLVER = new FaceParameterResolver();


    /**
     * 创建响应处理类
     * */
    public static CatServerResultHandler newResultHandler(AbstractResponesWrapper wrapperHandler){
        return new ResultWrapperHandler(wrapperHandler);
    }


    /**
     * 精简模式下参数预处理
     * 精简模式下，默认使用post+json请求。
     * 增强interface的方法有且仅有一个虚拟入参对象VirtualParameter
     * 方取arguments第一个元素，调用VirtualParameter.toArray转换为实际参数列表
     * */
    private static final class FaceParameterResolver implements CatParameterResolver {
        @Override
        public Object[] resolveArguments(CatMethodInfo methodInfo, Object[] arguments) throws Exception {
            if( arguments == null || arguments.length == 0 ){
                return arguments;
            }
            VirtualParameter requestBody = (VirtualParameter) arguments[0];
            Object[] args = requestBody.toArray();
            return args;
        }
    }




    /**
     * 有包装器类
     * */
    private static class ResultWrapperHandler implements CatServerResultHandler {

        private final AbstractResponesWrapper wrapperHandler;

        public ResultWrapperHandler(AbstractResponesWrapper wrapperHandler) {
            this.wrapperHandler = wrapperHandler;
        }

        @Override
        public Object onSuccess(Object value, Class returnType){
            if( value != null ){
                Class<?> returnClass = value.getClass();
                Class wrapperClass = wrapperHandler.getWrapperClass();
                if( wrapperClass.equals(returnClass) || wrapperClass.isAssignableFrom(returnClass)){
                    return value;
                } else {
                    return wrapperHandler.createEntryOnSuccess(value, returnType);
                }
            } else {
                return wrapperHandler.createEntryOnSuccess(value, returnType);
            }
        }

        @Override
        public Object onError(Throwable throwable, Class returnType) throws Throwable{
            return wrapperHandler.createEntryOnException(throwable, returnType);
        }
    }
}
