package com.bugcat.catserver.spi;


import com.bugcat.catserver.handler.CatInterceptPoint;

public class CatInterceptor {

    
    public static final CatInterceptor instance = new CatInterceptor();
    
    
    /**
     * 是否需要执行拦截器
     * 可以在此做权限验证
     * 如果抛出异常，后续都不会执行
     * */
    public boolean preHandle(CatInterceptPoint point) throws Throwable {
        return true;
    }

    
    /**
     * 在方法之前执行
     * 避免在此处抛出异常
     * 如果发生异常，切面方法、after都不会执行
     * */
    public void befor(CatInterceptPoint point) {
        
    }

    
    /**
     * 在方法之后执行
     * */
    public void after(CatInterceptPoint point){
        
    }
    
    
    /**
     * 如果befor、切面方法、after，发生了异常执行
     * 默认向上抛出
     * 如果忽略了异常，程序将继续执行
     * */
    public void exception(CatInterceptPoint point, Throwable ex) throws Throwable {
        throw ex;
    }


    public int getOrder(){
        return 0;
    }
    
    
}
