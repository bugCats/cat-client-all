package com.bugcat.catclient.spi;

import com.bugcat.catclient.beanInfos.CatClientInfo;
import com.bugcat.catclient.beanInfos.CatMethodInfo;
import com.bugcat.catclient.beanInfos.CatParameter;
import com.bugcat.catclient.config.CatHttpRetryConfigurer;
import com.bugcat.catclient.handler.CatHttpException;
import com.bugcat.catclient.handler.CatMethodInterceptor;
import com.bugcat.catclient.handler.ResultProcessor;
import com.bugcat.catclient.handler.SendProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 被 ApiMethod 标识的方法
 * 通过cglib生成代理类
 * 单例
 * @author bugcat
 * */
@Component
public class DefaultMethodInterceptor implements CatMethodInterceptor {

    
    @Autowired
    private CatHttpRetryConfigurer retryConfigurer;
    
    
    /**
     * 
     *  
     *  基础数据：基本数据类型、对应的包装类、String、Date、以及Number的其他子类
     * 
     *  有效参数：方法上“排除SendProcessor及其子类、和被@PathVariable标记参数”的其他参数
     *
     * 
     * 核心方法
     * 
     * 1、处理入参
     *   
     *   入参格式为：post、get键值对（form表单提交方式），或者使用post发送字符串
     *   
     *   因此需要将入参进行转换：
     *      
     *      如果方法上有多个有效入参，
     *          
     *          只能假设这些入参，全部是基础数据类型；（如果这些有效参数，存在对象，都会有bug。碰到这种情况，建议使用post发送字符串，或者将对象转成字符串，再使用）
     *      
     *          创建一个map，key=参数名称，value=参数值；（这就是基础数据类为什么要使用@RequestParam注解原因，interface编译成class之后，不保留参数名称）
     * 
     *          返回这个map，最后转成key=value&key2=value2键值对，或者json、xml字符串
     *          
     *      如果只有一个有效参数，
     *          
     *          同样创建一个map，key=参数名称，value=参数值；
     *          
     *          在判断这个参数数据类型，
     *          
     *              如果是基本数据，返回这个map
     *              
     *              如果是对象，将这个对象返回（此时返回的是对象，有别于上述的Map）
     *              
     *              最后将对象转换成key=value&key2=value2键值对，或者json、xml字符串
     *    
     *    
     *    
     * 2、发送前预处理
     *      
     *      设置远程服务的host、请求url、链接超时、日志方案、请求方式、请求头信息、签名
     *      
     *      转换入参，根据请求方式，将入参转换成最终形式：键值对 -> Map ；postString -> json、xml字符串
     * 
     * 
     * 
     * 3、发送http请求
     *    
     *      发送http
     *    
     *      根据日志方案，记录输入参数、响应参数、耗时
     *      
     * 
     * 
     * 4、处理http异常，判断是否可以重试
     * 
     *      设置了fallback，调用fallback对应的方法
     *      
     *      没有设置，调用默认处理方式
     *      
     *      
     * 
     * 5、解析http响应
     * 
     *      根据方法的返回参数信息，解析响应字符串
     *      
     *          返回参数：
     *              
     *              是基本数据，直接转换
     *          
     *              是复杂对象，通过fastjson转换
     *
     *              
     *              
     *              
     * 6、最后处理
     * 
     *      如果开启了包装器类模式，并且返回参数不等于包装器类，进行拆分、分离业务对象
     *    
     * 
     * 
     * 7、返回最终结果
     * 
     * 
     * */
    @Override
    public Object intercept(CatClientInfo catClientInfo, CatMethodInfo methodInfo, 
                            Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        //处理器，如果在@CatClient中指定了处理器，此处应该返回其子类
        CatClientFactory factory = methodInfo.getFactory();

        SendProcessor sendHandler = null;
        Integer handlerIndex = methodInfo.getHandlerIndex();
        if( handlerIndex != null ){ 
            //在方法上，传入了 SendHandler 或其子类
            sendHandler = (SendProcessor) args[handlerIndex];
            sendHandler.reset();
        } else {
            //否则通过工厂创建一个发送类
            sendHandler = factory.getSendHandler(); 
        }

        ResultProcessor resultHandler = factory.getResultHandler();
        
        //处理参数列表，如果存在PathVariable参数，将参数映射到url上
        CatParameter param = methodInfo.parseArgs(args);
        
        //设置http请求配置
        sendHandler.setConfigInfo(methodInfo, param);

        //设置参数，子类可以重写此方法，可以追加签名等信息
        sendHandler.setSendVariable(param);
        
        
        Object respObj = null;

        try {
            
            //执行发送http请求
            String respStr = doRequest(catClientInfo, sendHandler, resultHandler);
            
            //执行字符串转对象，此时对象，为方法的返回值类型
            respObj = resultHandler.resultToBean(respStr, sendHandler, catClientInfo, methodInfo);
        
        } catch ( Exception ex ) {

            //开启了异常回调模式
            if( catClientInfo.isFallbackMod() ){

                // 说明自定义了http异常处理类
                respObj = methodProxy.invokeSuper(target, args);
     
            } else {
                
                //执行默认的http异常处理类
                Object resp = resultHandler.onHttpError(ex, sendHandler, catClientInfo, methodInfo);
                if( resp != null  ){
                    if( resp instanceof String ){
                        respObj = resultHandler.resultToBean((String) resp, sendHandler, catClientInfo, methodInfo);
                    } else {
                        respObj = resp;
                    }
                } else {
                    respObj = null;
                }
            }
        }
        
        // 如果开启了包装器模式，拆包装
        return resultHandler.doFinally(respObj, sendHandler, catClientInfo, methodInfo);
        
    }

  
    /**
     * 执行http请求，如果开启了重连、并且满足重连设置，此处会循环调用，直至成功、或者重试次数耗尽
     * 重连次数，不包含第一次调用！
     * */
    private String doRequest(CatClientInfo clientInfo, SendProcessor sendHandler, ResultProcessor resultHandler) throws CatHttpException {
        try {
            String respStr = sendHandler.httpSend();
            return respStr;
        } catch ( CatHttpException ex ) {
            if ( resultHandler.canRetry(retryConfigurer, ex, clientInfo, sendHandler) ) {
                return doRequest(clientInfo, sendHandler, resultHandler);
            }
            throw ex;
        }
    }

}
