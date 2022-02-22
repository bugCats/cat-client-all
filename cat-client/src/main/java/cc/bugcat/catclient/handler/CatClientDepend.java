package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.config.CatHttpRetryConfigurer;
import cc.bugcat.catclient.scanner.CatClientDependFactoryBean;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatMethodSendInterceptor;
import cc.bugcat.catclient.spi.DefaultCatClientFactory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * CatClientInfoFactoryBean 相关依赖
 *
 * 组件加载依赖管理
 *
 * 加载顺序：
 *
 *  CatClientUtil
 *
 *  CatClientConfiguration、CatHttpRetryConfigurer
 *
 *  CatClientDependFactoryBean -> CatClientDepend
 *
 *  CatClientInfoFactoryBean -> catClient-interface
 *
 * @see CatClientDependFactoryBean
 * @author bugcat
 * */
public class CatClientDepend {

    /**
     * 全局默认的重连对象
     * */
    private final CatHttpRetryConfigurer retryConfigurer;

    /**
     * 全局默认的配置对象
     * */
    private final CatClientConfiguration clientConfig;

    /**
     * 全局Object的默认方法拦截器：toString、hashCode...
     * */
    private final MethodInterceptor defaultInterceptor;

    /**
     * 全局默认的client工厂
     * */
    private final CatClientFactory defaultClientFactory;

    /**
     * 全局默认的http方法拦截器
     * */
    private final CatMethodSendInterceptor defaultSendInterceptor;




    private CatClientDepend(Builder builder) {
        this.retryConfigurer = builder.retryConfigurer;
        this.clientConfig = builder.clientConfig;
        this.defaultInterceptor = builder.defaultInterceptor;
        this.defaultClientFactory = builder.defaultClientFactory;
        this.defaultSendInterceptor = builder.defaultSendInterceptor;
    }


    public CatHttpRetryConfigurer getRetryConfigurer() {
        return retryConfigurer;
    }

    public CatClientConfiguration getClientConfig() {
        return clientConfig;
    }

    public MethodInterceptor getDefaultInterceptor() {
        return defaultInterceptor;
    }

    public CatClientFactory getDefaultClientFactory() {
        return defaultClientFactory;
    }

    public CatMethodSendInterceptor getDefaultSendInterceptor() {
        return defaultSendInterceptor;
    }



    /**
     * 如果使用main方法调用，需要手动创建该对象
     * */
    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {

        private CatHttpRetryConfigurer retryConfigurer;

        private CatClientConfiguration clientConfig;

        private MethodInterceptor defaultInterceptor;

        private CatClientFactory defaultClientFactory;

        private CatMethodSendInterceptor defaultSendInterceptor;


        public Builder retryConfigurer(CatHttpRetryConfigurer retryConfigurer) {
            this.retryConfigurer = retryConfigurer;
            return this;
        }

        public Builder clientConfig(CatClientConfiguration clientConfig) {
            this.clientConfig = clientConfig;
            return this;
        }

        public Builder defaultClientFactory(CatClientFactory defaultClientFactory) {
            this.defaultClientFactory = defaultClientFactory;
            return this;
        }

        public Builder defaultSendInterceptor(CatMethodSendInterceptor defaultSendInterceptor) {
            this.defaultSendInterceptor = defaultSendInterceptor;
            return this;
        }

        public CatClientDepend build(){

            if( retryConfigurer == null ){
                retryConfigurer = new CatHttpRetryConfigurer();
            }

            if( clientConfig == null ){
                clientConfig = new CatClientConfiguration();
                clientConfig.afterPropertiesSet();
            }

            if( defaultInterceptor == null ){
                defaultInterceptor = new MethodInterceptor() {
                    @Override
                    public Object intercept (Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                        return methodProxy.invokeSuper(target, args);
                    }
                };
            }

            if( defaultSendInterceptor == null ){
                defaultSendInterceptor = new CatMethodSendInterceptor(){};
            }

            if( defaultClientFactory == null ){
                defaultClientFactory = new DefaultCatClientFactory();
            }

            return new CatClientDepend(this);
        }
    }


}
