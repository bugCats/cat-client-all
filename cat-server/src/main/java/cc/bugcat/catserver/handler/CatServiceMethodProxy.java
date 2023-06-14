package cc.bugcat.catserver.handler;


import cc.bugcat.catface.utils.CatToosUtil;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.reflect.FastMethod;

/**
 * 在controller的拦截器中，需要执行CatServer实现类方法。
 *
 * 如果Service实现类本身为cglib代理类，则直接执行cglib代理方法。
 *
 * 如果不是，使用FastClass方式调用，避免使用反射。
 *
 * 并且，在环绕在Service实现类方法上的切面，也可以正常执行。
 * */
class CatServiceMethodProxy {


    protected Object invokeProxy(Object target, Object[] args) throws Exception {
        return null;
    }


    
    public static CatServiceMethodProxy getCglibProxy(MethodProxy methodProxy){
        return new CglibServiceMethodProxy(methodProxy);
    }
    public static CatServiceMethodProxy getFastProxy(FastMethod fastMethod){
        return new OtherServiceMethodProxy(fastMethod);
    }
    

    /**
     * cglib代理
     * */
    private static class CglibServiceMethodProxy extends CatServiceMethodProxy {
        
        private final MethodProxy methodProxy;
        
        public CglibServiceMethodProxy(MethodProxy methodProxy) {
            this.methodProxy = methodProxy;
        }

        @Override
        protected Object invokeProxy(Object target, Object[] args) throws Exception {
            try {
                return methodProxy.invoke(target, args);
            } catch ( Throwable throwable ) {
                throw new Exception(CatToosUtil.getCause(throwable));
            }
        }
    }


    /**
     * 使用fastMethod代理
     * */
    private static class OtherServiceMethodProxy extends CatServiceMethodProxy {
        
        private final FastMethod fastMethod;
        
        public OtherServiceMethodProxy(FastMethod fastMethod) {
            this.fastMethod = fastMethod;
        }

        @Override
        protected Object invokeProxy(Object target, Object[] args) throws Exception {
            return fastMethod.invoke(target, args);
        }
    }

}
