package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.beanInfos.CatMethodInfo;
import cc.bugcat.catclient.beanInfos.CatParameter;
import cc.bugcat.catclient.spi.CatResultProcessor;
import cc.bugcat.catclient.spi.CatSendInterceptor;
import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.catface.handler.CatContextHolder;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;


/**
 * 通过cglib动态代理生成interface方法的实现
 *
 * @author bugcat
 * */
public final class CatMethodAopInterceptor implements MethodInterceptor {

    private final MethodHandle methodHandle;

    private final CatClientInfo clientInfo;
    private final CatMethodInfo methodInfo;

    private final CatSendInterceptor methodInterceptor;
    private final CatClientFactoryAdapter factoryAdapter;

    
    private CatMethodAopInterceptor(Builder builder){
        this.methodHandle = builder.methodHandle;
        this.clientInfo = builder.clientInfo;
        this.methodInfo = builder.methodInfo;
        this.methodInterceptor = builder.methodInterceptor;
        this.factoryAdapter = builder.factoryAdapter;
    }


    /**
     *
     * 执行CatClient-interface动态生成对象的方法
     *
     * 基础数据：基本数据类型、对应的包装类、String、Date、以及Number的其他子类
     * 有效参数：方法上“排除CatSendProcessor及其子类、和被@PathVariable、@RequestHeader标记参数”的其他参数
     *
     *
     * 核心方法
     *
     * @param target    cglib动态生成的CatClient-interface对象
     * @param method    interface的方法
     * @param args      调用参数列表
     * @param methodProxy   cglib封装的Method对象
     * */
    public Object intercept(Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        //处理器，如果在@CatClient中指定了处理器，此处应该返回其子类
        CatSendProcessor sendHandler = null;
        Integer handlerIndex = methodInfo.getHandlerIndex();
        if ( handlerIndex != null ) {
            //在方法上，传入了 SendHandler 或其子类
            sendHandler = (CatSendProcessor) args[handlerIndex];
        } else {
            //否则通过工厂创建一个发送类
            sendHandler = factoryAdapter.newSendHandler();
        }



        //原始响应字符串
        String respStr = null;

        //响应对象
        Object respObj = null;

        // 响应处理类
        CatResultProcessor resultHandler = factoryAdapter.getResultHandler();

        //处理参数列表，如果存在PathVariable参数，将参数映射到url上
        CatParameter parameter = methodInfo.parseArgs(args);

        // 将部分参数，存入ThreadLocal中，在fallback中，可以通过 CatClientContextHolder.getContextHolder 得到这些数据
        CatClientContextHolder context = CatClientContextHolder.builder()
                .sendHandler(sendHandler)
                .clientInfo(clientInfo)
                .methodInfo(methodInfo)
                .interceptor(methodInterceptor)
                .factoryAdapter(factoryAdapter)
                .build();

        try {
            //设置线程上下文
            CatContextHolder.setContext(context);

            //设置参数
            context.executeConfigurationResolver(parameter);

            //处理额外参数
            context.executeVariableResolver();

            //执行发送http请求
            respStr = doRequest(context, resultHandler);

            //执行字符串转对象，此时对象，为方法的返回值类型
            respObj = resultHandler.resultToBean(respStr, context);
            
        } catch ( Throwable throwable ) {

            // http异常，或者反序列化异常了
            CatContextHolder.setException(throwable);

            if ( clientInfo.isFallbackMod() ) { //开启了异常回调模式，执行自定义http异常处理
                if( method.isDefault() ){ // interface默认方法
                    try {
                        respObj = invokeDefaultMethod(target, method, args);
                        return respObj;
                    } catch ( Throwable ex ) {
                        // interface默认方法中继续抛出异常，不处理
                    }

                } else { // 具体回调类的方法
                    try {
                        respObj = methodProxy.invokeSuper(target, args);
                        return respObj;
                    } catch ( Throwable ex ) {
                        // 回调类中继续抛出异常，不处理
                    }
                }
            }

            //没有定义回调模式、或者回调模式继续抛出异常
            if ( respObj == null ) {

                //执行默认的http异常处理类
                boolean donext = resultHandler.onHttpError(context);
                Object result = context.getResult();

                if ( donext ) {
                    //返回true，会继续执行resultToBean、doFinally方法；

                    if ( result instanceof String ) {
                        respStr = (String) result;
                        respObj = resultHandler.resultToBean(respStr, context);
                    } else {
                        respObj = result;
                    }
                    
                } else {
                    // 返回false，则直接执行doFinally
                    
                    respObj = result;
                }
            }

        } finally {

            try {

                // 如果开启了包装器模式，拆包装
                respObj = resultHandler.onFinally(respObj, context);

            } catch ( Exception ex ) {

                // 拆包中出现异常
                throw ex;

            } finally {

                // 在最后的最后，打印日志，移除CatContextHolder对象
                context.printLog();
                CatContextHolder.remove();
            }
        }

        return respObj;
    }


    /**
     * 执行http请求，如果开启了重连、并且满足重连设置，此处会循环调用，直至成功、或者重试次数耗尽
     * 重连次数，不包含第一次调用！
     * */
    private String doRequest(CatClientContextHolder context, CatResultProcessor resultHandler) throws Throwable {
        try {
            String respStr = context.executeRequest();
            return respStr;
        } catch ( CatHttpException exception ) {
            if ( resultHandler.canRetry(exception, context) ) {
                return doRequest(context, resultHandler);
            }
            throw exception.getCause();
        }
    }


    /**
     * 执行interface默认方法
     * */
    private Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {
        return methodHandle.bindTo(proxy).invokeWithArguments(args);
    }




    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {

        /**
         * 执行interface默认方法
         * */
        private static Constructor<MethodHandles.Lookup> constructor;
        static {
            try {
                constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
                constructor.setAccessible(true);
            } catch ( Exception ex ) {
                ex.printStackTrace();
            }
        }

        private MethodHandle methodHandle;
        private CatClientInfo clientInfo;
        private CatMethodInfo methodInfo;
        private Method method;
        private CatSendInterceptor methodInterceptor;
        private CatClientFactoryAdapter factoryAdapter;

        
        public Builder clientInfo(CatClientInfo clientInfo) {
            this.clientInfo = clientInfo;
            return this;
        }
        
        public Builder methodInfo(CatMethodInfo methodInfo) {
            this.methodInfo = methodInfo;
            return this;
        }
        
        public Builder methodInterceptor(CatSendInterceptor methodInterceptor) {
            this.methodInterceptor = methodInterceptor;
            return this;
        }

        public Builder method(Method method) {
            this.method = method;
            return this;
        }

        public Builder factoryAdapter(CatClientFactoryAdapter factoryAdapter) {
            this.factoryAdapter = factoryAdapter;
            return this;
        }

        public CatMethodAopInterceptor build(){
            if ( method.isDefault() ) {
                try {
                    Class<?> declaringClass = method.getDeclaringClass();
                    this.methodHandle = constructor.newInstance(declaringClass,
                                    MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED |
                                    MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC)
                            .unreflectSpecial(method, declaringClass);
                } catch ( Exception ex ) {
                    ex.printStackTrace();
                }
            }
            return new CatMethodAopInterceptor(this);
        }
    }

}
