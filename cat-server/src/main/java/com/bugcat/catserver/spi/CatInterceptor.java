package com.bugcat.catserver.spi;


import com.bugcat.catserver.handler.CatInterceptPoint;
import com.bugcat.catserver.handler.CatMethodInterceptor;


/**
 * 
 * 拦截器
 * 
 * 被{@code CatServer}标记的类，会作为Controller注册到系统中，
 * 如果需要对这些类权限控制，可以通过Aop切面控制，也可以使用自定义拦截器处理
 * 
 * 注意，此拦截器只会通过http访问时生效，如果直接内部调用标记类，是不会生效！
 * 
 * @see CatMethodInterceptor
 * 
 * */
public class CatInterceptor {

    
    public static final CatInterceptor defaults = new CatInterceptor();
    
    
    /**
     * 是否需要执行拦截器
     * 
     * 某个拦截器，可能被多个{@code @CatServer}使用，
     * 但是该拦截器内容可能是按需执行，如果返回false，则不会加入到拦截器链中
     * 
     * 可以在此处做访问权限校验
     * 
     * 如果抛出异常，后续的befor、切面方法、after都不会执行。
     * */
    public boolean preHandle(CatInterceptPoint point) throws Throwable {
        return true;
    }

    
    /**
     * 在方法之前执行
     * 如果发生异常，后续的切面方法、after都不会执行
     * */
    public void befor(CatInterceptPoint point) throws Throwable {
        
    }

    
    /**
     * 在切面方法之后执行
     * 此处避免抛出异常
     * */
    public void after(CatInterceptPoint point){
        
    }
    
    
    /**
     * 如果befor、切面方法、after，发生了异常执行，默认继续向上抛出
     * 如果忽略了异常，程序将继续执行
     * */
    public void exception(CatInterceptPoint point, Throwable ex) throws Throwable {
        throw ex;
    }


    public int getOrder(){
        return 0;
    }
    
    
}
