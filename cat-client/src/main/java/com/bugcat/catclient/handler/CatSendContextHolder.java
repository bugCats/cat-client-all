package com.bugcat.catclient.handler;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

/**
 * @author bugcat
 * */
public class CatSendContextHolder{

    private static ThreadLocal<CatSendContextHolder> threadLocal = new ThreadLocal<>();

    /**
     *  仅当发生异常时，才会有值
     * */
    public static CatSendContextHolder getContext() {
        return threadLocal.get();
    }
    
    
    /**
     * http原始响应内容，不一定有值
     * */
    private final String response;
    
    /**
     * http、或者对象反序列化异常，一定不为null
     * */
    private final Exception exception;


    public CatSendContextHolder(String response, Exception exception) {
        this.exception = exception;
        this.response = response;
        threadLocal.set(this);
    }


    @Nullable
    public String getResponse() {
        return response;
    }

    @NotNull
    public Exception getException() {
        return exception;
    }

    
    public void remove() {
        threadLocal.remove();
    }
}
