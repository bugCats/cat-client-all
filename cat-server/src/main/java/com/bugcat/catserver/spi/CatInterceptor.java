package com.bugcat.catserver.spi;


import com.bugcat.catserver.handler.CatInterceptPoint;

public class CatInterceptor {

    
    public static final CatInterceptor instance = new CatInterceptor();
    
    
    /**
     * 是否需要执行拦截器
     * */
    public boolean preHandle(CatInterceptPoint point) {
        return true;
    }

    
    /**
     * 在方法之前执行
     * */
    public void befor(CatInterceptPoint point) {
        
    }

    
    /**
     * 在方法之后执行
     * */
    public void after(CatInterceptPoint point){
        
    }
    
    
    /**
     * 如果发生了异常
     * */
    public void exception(CatInterceptPoint point, Throwable ex) throws Throwable{
        throw ex;
    }


    public int getOrder(){
        return 0;
    }
    
    
}
