package com.bugcat.catclient.handler;

/**
 * http异常
 * */
public class CatHttpException extends Exception {

    private Integer status;
    private Exception exception;
    
    
    public CatHttpException(Integer status, Exception exception) {
        super(exception);
        this.status = status;
        this.exception = exception;
    }

    public Integer getStatus() {
        return status;
    }

    
    public Exception getIntrospectedException(){
        return exception;
    }
    
    public Class<? extends Exception> getIntrospectedClass() {
        return exception.getClass();
    }
    
}
