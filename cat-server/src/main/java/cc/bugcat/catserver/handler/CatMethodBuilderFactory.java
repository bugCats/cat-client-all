package cc.bugcat.catserver.handler;

import cc.bugcat.catserver.annotation.CatBefore;
import cc.bugcat.catserver.beanInfos.CatServerInfo;
import cc.bugcat.catserver.spi.CatParameterResolver;
import cc.bugcat.catserver.spi.CatServerResultHandler;
import cc.bugcat.catserver.utils.CatServerUtil;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

/**
 * @author bugcat
 * */
public class CatMethodBuilderFactory {


    /**
     * 标记的@CatServer注解信息
     * */
    private CatServerInfo serverInfo;

    /**
     * 被@CatServer标记的server对象
     * */
    private Object serverBean;

    /**
     * server对象的class
     * 如果server对象被其他组件动态代理，则为代理后的class
     * serverBeanClass 不一定等于 serverClass
     * */
    private Class serverBeanClass;

    /**
     * 原interface的方法元数据
     * */
    private StandardMethodMetadata interMethodMetadata;

    /**
     * 原server类的方法
     * */
    private Method serverMethod;

    /**
     * cglib 的动态调用类
     * */
    private FastClass fastClass;


    /**
     * 精简模式下参数预处理
     * */
    private CatParameterResolver parameterResolver;



    private CatMethodInfo.Builder methodBuilder;


    private CatMethodBuilderFactory(){}


    public static CatMethodBuilderFactory newFactory(){
        CatMethodBuilderFactory factory = new CatMethodBuilderFactory();
        factory.methodBuilder = CatMethodInfo.builder();
        return factory;
    }


    /**
     * 注解对象
     * */
    public CatMethodBuilderFactory serverInfo(CatServerInfo serverInfo){
        this.methodBuilder.serverInfo(serverInfo);
        this.serverInfo = serverInfo;
        return this;
    }

    /**
     * 被@CatServer标记的类
     * */
    public CatMethodBuilderFactory serverClass(Class serverClass){
        this.serverBean = CatServerUtil.getBean(serverClass);
        this.serverBeanClass = serverBean.getClass();
        if( !ClassUtils.isCglibProxy(serverBeanClass) ){// server对象，没有、或者不是cglib代理，使用快速处理类
            this.fastClass = FastClass.create(serverBeanClass);
        }
        return this;
    }



    /**
     * 原interface方法
     * */
    public CatMethodBuilderFactory interMethodMetadata(StandardMethodMetadata interMethod){
        this.interMethodMetadata = interMethod;
        return this;
    }

    /**
     * 原server类方法
     * */
    public CatMethodBuilderFactory serverMethod(Method serverMethod){
        this.serverMethod = serverMethod;
        return this;
    }

    /**
     * 参数预处理器
     * */
    public CatMethodBuilderFactory parameterResolver(CatParameterResolver parameterResolver){
        this.parameterResolver = parameterResolver;
        return this;
    }


    /**
     * 创建controller方法拦截器
     * */
    public CatMethodAopInterceptor build(){

        // 原interface的方法
        methodBuilder.interMethod(interMethodMetadata)
                .serviceMethodProxy(fastClass, serverBeanClass)
                .parameterResolver(parameterResolver)
                .serverMethod(serverMethod);

        CatServerResultHandler resultHandler = serverInfo.getServerConfig().getResultHandler(serverInfo.getWrapperHandler());

        CatParameterResolver argumentResolver = null;
        CatBefore catBefore = serverMethod.getAnnotation(CatBefore.class);
        if( catBefore != null ){
            Class<? extends CatParameterResolver> resolverClass = catBefore.value();
            argumentResolver = CatServerUtil.getBean(resolverClass);
        } else {
            argumentResolver = CatServerDefaults.DEFAULT_RESOLVER;
        }

        CatMethodAopInterceptor.Builder builder = CatMethodAopInterceptor.builder();
        builder.serverInfo(serverInfo);
        builder.serverBean(serverBean);
        builder.argumentResolver(argumentResolver);
        builder.resultHandler(resultHandler);
        builder.methodInfo(methodBuilder.build());

        return builder.build();
    }



}
