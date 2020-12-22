package com.bugcat.catclient.handler;


import com.bugcat.catclient.beanInfos.CatMethodInfo;
import com.bugcat.catclient.beanInfos.CatClientInfo;

/**
 * http响应处理类
 * @author bugcat
 * */
public abstract class ResultProcessor {



    /**
     * 如果发生了40x、50x等异常，默认的异常处理方式
     * 如果结果返回String，会继续执行 resultToBean、doFinally 方法；否则直接执行atLast
     * @return String => 执行 resultToBean、doFinally、atLast 方法；
     *         Bean   => 直接执行 atLast 方法；
     *
     * */
    public abstract Object onHttpError(Exception exception, SendProcessor sendHandler, CatClientInfo catClientInfo, CatMethodInfo methodInfo) throws Exception;



    /**
     * 将返回字符串，转换成对象
     * @return
     */
    public abstract Object resultToBean(String resp, SendProcessor sendHandler, CatClientInfo catClientInfo, CatMethodInfo methodInfo);

    
    /**
     * 在toBean之后执行
     * 获取到对象之后，再做处理
     * */
    public Object doFinally(Object resp, SendProcessor sendHandler, CatClientInfo catClientInfo, CatMethodInfo methodInfo) throws Exception {
        return resp;
    }



}
