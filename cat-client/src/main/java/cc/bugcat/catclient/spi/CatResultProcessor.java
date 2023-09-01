package cc.bugcat.catclient.spi;


import cc.bugcat.catclient.handler.CatClientContextHolder;
import cc.bugcat.catclient.exception.CatHttpException;
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
    boolean canRetry(CatHttpException exception, CatClientContextHolder context);


    /**
     * 将http返回的字符串，转换成对象
     * */
    Object resultToBean(String result, CatClientContextHolder context) throws Exception;


    /**
     * 如果发生了40x、50x等异常处理方式，默认继续抛出；
     * 可以给CatClientContextHolder.responseObject赋默认值。
     * @return true，执行{@link CatResultProcessor#resultToBean(String, CatClientContextHolder)}方法；false 跳过；
     * */
    boolean onHttpError(CatClientContextHolder context) throws Throwable;

    
    /**
     * 在resultToBean之后执行
     * 获取到对象之后，再进行拆包装器处理{@link AbstractResponesWrapper}
     *
     * @param result 经过resultToBean方法转换后的参数
     * */
    Object onFinally(Object result, CatClientContextHolder context) throws Exception;



}
