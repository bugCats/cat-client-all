package cc.bugcat.example.asm;


import cc.bugcat.catclient.beanInfos.CatMethodInfo;
import cc.bugcat.catclient.handler.CatMethodAopInterceptor;
import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.example.catserver.server.ApiRemote1;
import com.alibaba.fastjson.JSONObject;
import org.springframework.cglib.proxy.CallbackHelper;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Date;

public class DemoUser {


    public static void main(String[] args) {


        Class[] interfaces = new Class[]{ApiRemote1.class};

        CallbackHelper helper = new CallbackHelper(Object.class, interfaces) {

            @Override
            protected Object getCallback (Method method) {
                return new MethodInterceptor(){
                    @Override
                    public Object intercept(Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                        if ( method.isDefault() ) {
                            return invokeDefaultMethod(target, method, args);
                        } else {
                            return methodProxy.invokeSuper(target, args);
                        }
                    }
                };
            }
        };

        Enhancer enhancer = new Enhancer();
        enhancer.setInterfaces(interfaces);
        enhancer.setSuperclass(Object.class);
        enhancer.setCallbackFilter(helper);
        enhancer.setCallbacks(helper.getCallbacks());
        ApiRemote1 obj = (ApiRemote1) enhancer.create();

        System.out.println(JSONObject.toJSONString(obj.demo1(null)));

        System.out.println(obj.demo2(null));

        System.out.println(obj.demo3(null));


    }

    private static Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {
        final Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                .getDeclaredConstructor(Class.class, int.class);
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        System.out.println(MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
                | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC);

        final Class<?> declaringClass = method.getDeclaringClass();
        return constructor
                .newInstance(declaringClass, 15)
                .unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args);
    }

}
