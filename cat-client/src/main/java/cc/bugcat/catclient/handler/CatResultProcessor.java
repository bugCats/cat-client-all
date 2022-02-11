package cc.bugcat.catclient.handler;


import cc.bugcat.catface.spi.AbstractResponesWrapper;

/**
 * http响应处理类
 *
 * @author bugcat
 * */
public interface CatResultProcessor {



    /**
     * 发生http异常，是否重连
     * */
    boolean canRetry(CatHttpException exception, CatSendContextHolder context);



    /**
     * 如果发生了40x、50x等异常处理方式。默认继续抛出；
     * 可以给 CatSendContextHolder.result 赋默认值
     * 如果结果返回true，会继续执行 resultToBean、doFinally 方法；
     * 返回false，则直接执行doFinally
     * @return true => 执行 resultToBean、doFinally 方法；
     *         false => 直接执行 doFinally 方法；
     * */
    boolean onHttpError(CatSendContextHolder context) throws Exception;



    /**
     * 将http返回的字符串，转换成对象
     * */
    Object resultToBean(String result, CatSendContextHolder context);



    /**
     * 在resultToBean之后执行
     * 获取到对象之后，再进行拆包装器处理{@link AbstractResponesWrapper}
     *
     * @param resp 经过resultToBean方法转换后的参数
     * */
    Object doFinally(Object resp, CatSendContextHolder context) throws Exception;



}