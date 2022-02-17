package cc.bugcat.catserver.handler;

import cc.bugcat.catserver.asm.CatVirtualParameterEnhancer.VirtualParameter;


/**
 * controller入参预处理
 * */
abstract class CatArgumentResolver {


    protected abstract Object[] resolveArguments(Object[] args) throws Exception;



    /**
     * 精简模式需要从Request中读流方式，获取到入参
     * 入参为json字符串，再解析成入参对象
     * */
    protected static class CatFaceResolver extends CatArgumentResolver {

        @Override
        protected Object[] resolveArguments(Object[] arguments) throws Exception {
            if( arguments == null || arguments.length == 0 ){
                return arguments;
            }
            VirtualParameter requestBody = (VirtualParameter) arguments[0];
            Object[] args = requestBody.toArray();
            return args;
        }
    }


    /**
     * 默认处理器
     * */
    protected static class CatDefaultResolver extends CatArgumentResolver {
        @Override
        public Object[] resolveArguments(Object[] args) throws Exception {
            return args;
        }
    }


}
