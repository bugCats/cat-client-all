package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.beanInfos.CatMethodInfo;
import cc.bugcat.catclient.beanInfos.CatParameter;
import cc.bugcat.catclient.config.CatHttpRetryConfigurer;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatMethodSendInterceptor;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;


/**
 * 通过cglib动态代理
 *
 * @author bugcat
 * */
public final class CatMethodAopInterceptor implements MethodInterceptor {

    private final CatClientInfo clientInfo;
    private final CatMethodInfo methodInfo;

    private final CatMethodSendInterceptor methodInterceptor;
    private final CatHttpRetryConfigurer retryConfigurer;
    private final CatClientFactory clientFactory;

    private CatMethodAopInterceptor(Builder builder){
        this.clientInfo = builder.clientInfo;
        this.methodInfo = builder.methodInfo;
        this.methodInterceptor = builder.methodInterceptor;
        this.retryConfigurer = builder.retryConfigurer;
        this.clientFactory = builder.clientFactory;
    }


    /**
     *
     * 基础数据：基本数据类型、对应的包装类、String、Date、以及Number的其他子类
     * 有效参数：方法上“排除CatSendProcessor及其子类、和被@PathVariable、@RequestHeader标记参数”的其他参数
     *
     *
     * 核心方法
     *
     * 1、处理入参
     *      入参格式为：post、get键值对（form表单提交方式），或者使用post发送字符串
     *      因此需要将入参进行转换：
     *          如果方法上有多个有效入参，
     *              只能假设这些入参，全部是基础数据类型；（如果这些有效参数，存在对象，都会有bug。碰到这种情况，建议使用post发送字符串，或者将对象转成字符串，再使用）
     *              创建一个map，key=参数名称，value=参数值；（这就是基础数据类为什么要使用@RequestParam注解原因，interface编译成class之后，不保留参数名称）
     *              返回这个map，最后转成key=value&key2=value2键值对，或者json、xml字符串
     *
     *          如果只有一个有效参数，
     *              同样创建一个map，key=参数名称，value=参数值；
     *              在判断这个参数数据类型，
     *
     *          如果是基本数据，返回这个map
     *
     *          如果是对象，将这个对象返回（此时返回的是对象，有别于上述的Map）
     *              最后将对象转换成key=value&key2=value2键值对，或者json、xml字符串
     *
     *
     * 2、发送前预处理
     *      设置远程服务的host、请求url、链接超时、日志方案、请求方式、请求头信息、签名
     *      转换入参，根据请求方式，将入参转换成最终形式：键值对 -> Map ；postString -> json、xml字符串
     *
     *
     * 3、发送http请求
     *      发送http，根据日志方案，记录输入参数、响应参数、耗时
     *
     *
     * 4、处理http异常，判断是否可以重试
     *      设置了fallback，调用fallback对应的方法
     *      没有设置，调用默认处理方式
     *
     *
     * 5、解析http响应
     *      根据方法的返回参数信息，解析响应字符串
     *      返回参数：
     *          是基本数据，直接转换
     *          是复杂对象，通过转换器转换
     *
     *
     * 6、最后处理
     *      如果开启了包装器类模式，并且返回参数不等于包装器类，进行拆分、分离业务对象
     *
     * 7、返回最终结果
     *
     */

    public Object intercept(Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        //处理器，如果在@CatClient中指定了处理器，此处应该返回其子类

        CatSendProcessor sendHandler = null;
        Integer handlerIndex = methodInfo.getHandlerIndex();
        if ( handlerIndex != null ) {
            //在方法上，传入了 SendHandler 或其子类
            sendHandler = (CatSendProcessor) args[handlerIndex];
        } else {
            //否则通过工厂创建一个发送类
            sendHandler = clientFactory.newSendHandler();
        }

        // 响应处理类
        CatResultProcessor resultHandler = clientFactory.getResultHandler();

        //处理参数列表，如果存在PathVariable参数，将参数映射到url上
        CatParameter parameter = methodInfo.parseArgs(args);

        // 将部分参数，存入ThreadLocal中，在fallback中，可以通过 CatSendContextHolder.getContextHolder 得到这些数据
        CatSendContextHolder context = CatSendContextHolder.builder()
                .sendHandler(sendHandler)
                .clientInfo(clientInfo)
                .methodInfo(methodInfo)
                .interceptor(methodInterceptor)
                .clientFactory(clientFactory)
                .retryConfigurer(retryConfigurer)
                .build();

        //设置参数
        sendHandler.sendConfigurationResolver(context, parameter);

        //处理额外参数
        context.executeVariable();

        //原始响应字符串
        String respStr = null;

        //响应对象
        Object respObj = null;

        try {

            //执行发送http请求
            respStr = doRequest(context, resultHandler);

            //执行字符串转对象，此时对象，为方法的返回值类型
            respObj = resultHandler.resultToBean(respStr, context);

        } catch ( Exception ex ) {

            // http异常，或者反序列化异常了
            context.setException(ex);

            //开启了异常回调模式
            if ( clientInfo.isFallbackMod() ) {

                // 说明自定义了http异常处理类
                respObj = methodProxy.invokeSuper(target, args);
            }

            //没有定义回调模式、或者回调模式返回null
            if ( respObj == null ) {

                //执行默认的http异常处理类
                boolean donext = resultHandler.onHttpError(context);

                //返回true，会继续执行 resultToBean、doFinally 方法；返回false，则直接执行doFinally
                if ( donext ) {
                    Object result = context.getResult();
                    if ( result instanceof String ) {
                        respStr = (String) result;
                        respObj = resultHandler.resultToBean(respStr, context);
                    } else {
                        respObj = result;
                    }
                } else {
                    respObj = null;
                }
            }

        } finally {
            try {
                // 如果开启了包装器模式，拆包装
                respObj = resultHandler.doFinally(respObj, context);
            } catch ( Exception ex ) {
                // 拆包中出现异常
                throw ex;
            } finally {
                // 在最后的最后，打印日志，移除CatSendContextHolder对象
                context.printLog();
                context.remove();
            }
        }
        return respObj;
    }


    /**
     * 执行http请求，如果开启了重连、并且满足重连设置，此处会循环调用，直至成功、或者重试次数耗尽
     * 重连次数，不包含第一次调用！
     */
    private String doRequest(CatSendContextHolder context, CatResultProcessor resultHandler) throws Exception {
        try {
            String respStr = context.executeRequest();
            return respStr;
        } catch ( CatHttpException exception ) {
            if ( resultHandler.canRetry(exception, context) ) {
                return doRequest(context, resultHandler);
            }
            throw exception.getIntrospectedException();
        }
    }






    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {

        private CatClientInfo clientInfo;
        private CatMethodInfo methodInfo;
        private CatMethodSendInterceptor methodInterceptor;
        private CatHttpRetryConfigurer retryConfigurer;
        private CatClientFactory clientFactory;

        public Builder clientInfo(CatClientInfo clientInfo) {
            this.clientInfo = clientInfo;
            return this;
        }
        public Builder methodInfo(CatMethodInfo methodInfo) {
            this.methodInfo = methodInfo;
            return this;
        }
        public Builder methodInterceptor(CatMethodSendInterceptor methodInterceptor) {
            this.methodInterceptor = methodInterceptor;
            return this;
        }
        public Builder retryConfigurer(CatHttpRetryConfigurer retryConfigurer) {
            this.retryConfigurer = retryConfigurer;
            return this;
        }
        public Builder clientFactory(CatClientFactory clientFactory) {
            this.clientFactory = clientFactory;
            return this;
        }

        public CatMethodAopInterceptor build(){
            return new CatMethodAopInterceptor(this);
        }
    }

}
