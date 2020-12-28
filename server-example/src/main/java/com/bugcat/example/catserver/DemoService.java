package com.bugcat.example.catserver;

import com.bugcat.catserver.asm.CatAsm;
import com.bugcat.example.api.UserService;
import com.bugcat.example.dto.Demo;
import com.bugcat.example.tools.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.DebuggingClassWriter;
import org.springframework.cglib.proxy.CallbackHelper;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;


/**
 * 
 * 一个普通的服务类
 * @author bugcat
 * 
 * */
@Service
public class DemoService {

    /**
     * 也可以作为普通组件自动注入
     * */
    @Autowired
    private UserService userService;
    
    
    public Demo creart(){
        Demo demo = new Demo();
        demo.setId(2L);
        demo.setName("bugcat");
        demo.setMark("服务端");
        return demo;
    }




    public static void main(String[] args) throws Exception {
        
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "F:\\tmp");

        CatAsm im = new CatAsm(ClassLoader.getSystemClassLoader());
        Class clazz = im.enhancer(UserService.class, ResponseEntity.class);

        for ( Method method : clazz.getMethods() ) {
            Annotation[] annotations = method.getAnnotations();
            System.out.println(method.getName() + " > " + annotations.length);
            Annotation[][] parameters = method.getParameterAnnotations();
            System.out.println(parameters.length);
        }
        

    }


    
    public static class UserCallbackHelper extends CallbackHelper {
        
        public UserCallbackHelper(Class superclass, Class[] interfaces) {
            super(superclass, interfaces);
        }

        @Override
        protected Object getCallback (Method method) {
            return new MethodInterceptor() {    //默认方法
                @Override
                public Object intercept (Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                    return methodProxy.invokeSuper(target, args);
                }
            };
        }
    }
    
}
