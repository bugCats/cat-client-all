package cc.bugcat.catserver.handler;

import cc.bugcat.catserver.asm.CatVirtualParameterEnhancer;


/**
 * 如果是精简模式情况，需要从Request中读流方式，获取到入参
 * 入参为json字符串，再解析成入参对象
 * */
public final class CatFaceResolver {
    
    private int parameterCount;
    
    
    protected CatFaceResolver(CatFaceResolverBuilder builder) {
        this.parameterCount = builder.getMethod().getParameterCount();
    }
    

    public Object[] resolveArgument(Object[] args) throws Exception {
        if( args == null || args.length == 0 ){
            return args;
        }
        Object[] result = new Object[parameterCount];
        CatVirtualParameterEnhancer.VirtualParameter body = (CatVirtualParameterEnhancer.VirtualParameter) args[0];
        for ( int i = 0; i < parameterCount; i++ ) {
            Object arg = body.invoke(i);
            result[i] = arg;
        }
        return result;
    }
    
}
