package cc.bugcat.catserver.handler;

import cc.bugcat.catserver.beanInfos.CatServerInfo;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.spi.CatInterceptorGroup;
import cc.bugcat.catserver.spi.CatParameterResolver;
import cc.bugcat.catserver.spi.CatResultHandler;
import cc.bugcat.catserver.spi.CatServerInterceptor;
import cc.bugcat.catserver.utils.CatServerUtil;
import org.springframework.asm.Type;
import org.springframework.cglib.core.Signature;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author bugcat
 * */
public final class CatMethodInfoBuilder {


    /**
     * serverBean的class。
     * 如果server对象被其他组件动态代理，则为代理后的class！
     * serverBeanClass 不一定等于 serverClass
     * */
    private final Class serverBeanClass;
    
    /**
     * cglib 的动态调用类
     * */
    private FastClass fastClass;

    /**
     * {@code @CatServer}配置的拦截器
     * */
    protected List<CatServerInterceptor> interceptors;
    
    /**
     * 配置的结果处理类
     * */
    protected CatResultHandler resultHandler;
    
    /**
     * 运行时拦截器组
     * @see CatServerConfiguration#getInterceptorGroup()
     * */
    protected List<CatInterceptorGroup> interceptorGroups;
    
    
    private CatMethodInfoBuilder(Object serverBean, CatServerInfo serverInfo){
        this.serverBeanClass = serverBean.getClass();
        if( ClassUtils.isCglibProxy(serverBeanClass) == false ){// server对象，没有、或者不是cglib代理，使用快速处理类
            this.fastClass = FastClass.create(serverBeanClass);
        }
        this.resultHandler = serverInfo.getResultHandler();
        this.parseInterceptor(serverInfo);
    }
    
    /**
     * 获取拦截器
     * */
    private void parseInterceptor(CatServerInfo serverInfo) {

        CatServerConfiguration serverConfig = serverInfo.getServerConfig();
        List<CatInterceptorGroup> interceptorGroup = new ArrayList<>(serverConfig.getInterceptorGroup()); //拦截器组

        boolean userOff = false;
        boolean groupOff = false;
        Set<Class<? extends CatServerInterceptor>> interceptorSet = new LinkedHashSet<>();
        for ( Class<? extends CatServerInterceptor> interceptor : serverInfo.getInterceptors() ) {
            if( interceptorSet.contains(interceptor) ){
                interceptorSet.remove(interceptor);
            }
            if( CatServerInterceptor.Empty.class == interceptor ){
                userOff = true;
                continue;
            } else if ( CatServerInterceptor.GroupOff.class == interceptor ){
                groupOff = true;
                continue;
            }
            interceptorSet.add(interceptor);
        }

        if( groupOff ){ //关闭拦截器组
            interceptorGroup.clear();
        }
        
        List<CatServerInterceptor> handers = null;
        if( userOff ){ //关闭自定义和全局
            handers = new ArrayList<>(0);
            
        } else { //启用自定义、全局拦截器
            
            handers = new ArrayList<>(interceptorSet.size() + 1);
            for ( Class<? extends CatServerInterceptor> clazz : interceptorSet ) {
                if (CatServerInterceptor.class.equals(clazz) ) {
                    // 默认拦截器，使用CatServerConfiguration.getGlobalInterceptor()替换
                    handers.add(serverConfig.getServerInterceptor());

                } else {
                    // CatServer上自定义拦截器
                    handers.add(CatServerUtil.getBean(clazz));
                }
            }
            if( handers.size() == 0 ){ //如果没有配置拦截器，添加全局
                handers.add(serverConfig.getServerInterceptor());
            }
        }
        
        Collections.sort(interceptorGroup, Comparator.comparingInt(CatInterceptorGroup::getOrder));
        this.interceptors = handers;
        this.interceptorGroups = interceptorGroup;
    }




    public static CatMethodInfoBuilder builder(Object serverBean, CatServerInfo serverInfo){
        return new CatMethodInfoBuilder(serverBean, serverInfo);
    }



    /**
     * 原interface的方法
     * */
    protected StandardMethodMetadata interMethod;

    /**
     * cglib生成的ctrl类方法
     * */
    protected Method controllerMethod;
    
    
    /**
     * 原server类的方法
     * */
    protected Method serverMethod;

    /**
     * controller快速调用server对象方法的
     * */
    protected CatServiceMethodProxy serviceMethodProxy;

    /**
     * 精简模式下参数预处理器
     * */
    protected CatParameterResolver parameterResolver;


    /**
     * 原interface的方法
     * */
    public CatMethodInfoBuilder interMethod(StandardMethodMetadata interMethod) {
        this.interMethod = interMethod;
        
        Method method = interMethod.getIntrospectedMethod();
        if( fastClass != null ){
            // server对象，没有、或者不是cglib代理，使用快速处理类
            FastMethod fastMethod = fastClass.getMethod(method);
            this.serviceMethodProxy = CatServiceMethodProxy.getFastProxy(fastMethod);
        } else {
            // server对象，被cglib代理
            MethodProxy proxy = MethodProxy.find(serverBeanClass, new Signature(method.getName(), Type.getMethodDescriptor(method)));
            this.serviceMethodProxy = CatServiceMethodProxy.getCglibProxy(proxy);
        }
        return this;
    }
    
    /**
     * cglib生成的ctrl类方法
     * */

    public CatMethodInfoBuilder controllerMethod(Method controllerMethod) {
        this.controllerMethod = controllerMethod;
        return this;
    }
    
    /**
     * server类的方法
     * */
    public CatMethodInfoBuilder serverMethod(Method serverMethod) {
        this.serverMethod = serverMethod;
        return this;
    }
    
    /**
     * 方法入参处理器
     * */
    public CatMethodInfoBuilder parameterResolver(CatParameterResolver parameterResolver) {
        this.parameterResolver = parameterResolver;
        return this;
    }

    
    /**
     * 生成方法描述信息对象，之后把临时缓存清空
     * */
    public CatMethodInfo build(){
        CatMethodInfo info = new CatMethodInfo(this);
        this.interMethod = null;
        this.serverMethod = null;
        this.serviceMethodProxy = null;
        this.parameterResolver = null;
        return info;
    }

}
