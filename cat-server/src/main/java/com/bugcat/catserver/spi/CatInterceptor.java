package com.bugcat.catserver.spi;


import com.bugcat.catserver.handler.CatInterceptPoint;

public interface CatInterceptor {
    
    final class Defualt implements CatInterceptor {
        
        public final static Defualt instance = new Defualt();

        @Override
        public boolean preHandle(CatInterceptPoint point) {
            return true;
        }

        @Override
        public void befor(CatInterceptPoint point) throws Exception {

        }

        @Override
        public void after(CatInterceptPoint point) throws Exception {
            if( point.getException() != null ){
                throw point.getException();
            }
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }

    
    
    boolean preHandle(CatInterceptPoint point);

    void befor(CatInterceptPoint point) throws Exception;

    void after(CatInterceptPoint point) throws Exception;
    
    int getOrder();
    
    
}
