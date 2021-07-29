package cc.bugcat.catserver.handler;

import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catserver.beanInfos.CatServerInfo;
import cc.bugcat.catserver.spi.CatInterceptor;
import cc.bugcat.catserver.utils.CatServerUtil;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * 通过cglib生成代理类
 * 单例
 *
 * @author bugcat
 */
public final class CatMethodInterceptor implements MethodInterceptor{
   
    /**
     * 原始server-interface实现类对象
     * 如果使用了代理，那么为代理后对象
     * */
    private final Object server;
    
    /**
     * 原始server-interface的方法
     * */
    private final StandardMethodMetadata interMethod;
    
    /**
     *  server对象，对应的方法
     * */
    private final CatMethodInterceptorBuilder.ServiceProxy realMethodproxy;

    
    private final List<CatInterceptor> handers;

    private final CatFaceResolver argumentResolver;    //参数预处理器

    private final Function<Object, Object> successToEntry;
    private final Function<Throwable, Object> errorToEntry;

    
    public CatMethodInterceptor(CatMethodInterceptorBuilder builder) {

        CatServerInfo serverInfo = builder.getServerInfo();
        Method realMethod = builder.getRealMethod();

        Class<? extends CatInterceptor>[] handerList = serverInfo.getHanders();
        List<CatInterceptor> handers = new ArrayList<>(handerList.length);
        for ( Class<? extends CatInterceptor> clazz : handerList ) {
            if ( CatInterceptor.class.equals(clazz) ) {
                handers.add(CatInterceptor.defaults);
            } else {
                handers.add(CatServerUtil.getBean(clazz));
            }
        }
        handers.sort(Comparator.comparingInt(CatInterceptor::getOrder));
        
        this.server = builder.getServerBean();
        this.realMethodproxy = builder.getServiceProxy();
        this.handers = handers;
        this.interMethod = builder.getInterMethodMetadata();
        this.argumentResolver = builder.getArgumentResolver();
        
        Class wrap = serverInfo.getWarpClass();
        if ( wrap != null ) {
            Class<?> returnType = realMethod.getReturnType();
            if ( wrap.equals(returnType) || wrap.isAssignableFrom(returnType.getClass()) ) {
                successToEntry = value -> value;
                errorToEntry = value -> value;
            } else {
                AbstractResponesWrapper wrapper = serverInfo.getWarp();
                successToEntry = value -> wrapper.createEntryOnSuccess(value, realMethod.getGenericReturnType());
                errorToEntry = value -> wrapper.createEntryOnException(value, realMethod.getGenericReturnType());
            }
        } else {
            successToEntry = value -> value;
            errorToEntry = value -> value;
        }
    }

    
    @Override
    public Object intercept(Object ctrl, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        HttpServletResponse response = attr.getResponse();

        if( argumentResolver != null ){
            args = argumentResolver.resolveArgument(args);
        }
        CatInterceptPoint point = new CatInterceptPoint(request, response, server, interMethod, args);

        List<CatInterceptor> active = new ArrayList<>(handers.size());
        for ( CatInterceptor hander : handers ) {
            if ( hander.preHandle(point) ) {
                active.add(hander);
            }
        }

        Throwable exception = null;

        try {

            for ( CatInterceptor hander : active ) {
                hander.befor(point);
            }

            point.result = successToEntry.apply(realMethodproxy.invoke(server, args));

            for ( int i = active.size() - 1; i >= 0; i-- ) {
                CatInterceptor hander = active.get(i);
                hander.after(point);
            }

        } catch ( Throwable ex ) {

            exception = ex;
            point.result = errorToEntry.apply(ex);

        } finally {

            if ( exception != null ) {
                if ( active.size() > 0 ) {
                    for ( int i = active.size() - 1; i >= 0; i-- ) {
                        CatInterceptor hander = active.get(i);
                        hander.exception(point, exception);
                    }
                } else {
                    throw exception;
                }
            }
        }

        return point.result;
    }

}
