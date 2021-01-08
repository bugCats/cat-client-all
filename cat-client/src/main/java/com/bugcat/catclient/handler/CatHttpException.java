package com.bugcat.catclient.handler;


public class CatHttpException extends Exception {

    private Integer status;
    private Throwable throwable;
    
    
    public CatHttpException(Integer status, Throwable throwable) {
        super(throwable);
        this.status = status;
        this.throwable = throwable;
    }

    public Integer getStatus() {
        return status;
    }

    public Throwable getThrowable() {
        return throwable;
    }
    
    
    public String throwableName(){
        return getThrowable().getClass().getName();
    }
}
