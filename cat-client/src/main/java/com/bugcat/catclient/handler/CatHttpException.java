package com.bugcat.catclient.handler;

/**
 * http异常
 * */
public class CatHttpException extends Exception {

    private Integer status;
    private Class<? extends Throwable> clazz;
    
    
    public CatHttpException(Integer status, Throwable throwable) {
        super(throwable);
        this.status = status;
        this.clazz = throwable.getClass();
    }

    public Integer getStatus() {
        return status;
    }

    public Class<? extends Throwable> getIntrospectedClass() {
        return clazz;
    }
    
}
