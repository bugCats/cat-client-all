package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.annotation.EnableCatClient;
import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.config.CatHttpRetryConfigurer;
import cc.bugcat.catclient.scanner.CatClientDependFactoryBean;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatClientMockProvide;
import cc.bugcat.catclient.spi.CatSendInterceptor;
import cc.bugcat.catclient.spi.SimpleClientFactory;
import cc.bugcat.catface.handler.EnvironmentAdapter;
import cc.bugcat.catface.utils.CatToosUtil;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * CatClientInfoFactoryBean 相关依赖，单例。
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
     * 可以在{@link EnableCatClient}指定
     * */
    private final CatClientConfiguration clientConfig;

    /**
     * Object的默认方法拦截器：toString、hashCode...
     * */
    private final MethodInterceptor superObjectInterceptor;

    /**
     * 全局默认的client工厂
     * */
    private final CatClientFactory clientFactory;

    /**
     * 全局默认的http方法拦截器
     * */
    private final CatSendInterceptor sendInterceptor;

    /**
     * 环境变量
     * */
    private final EnvironmentAdapter environment;
    
    /**
     * mock类
     * */
    private final Map<Class, Class> mockProvideMap;
    
    private CatClientDepend(Builder builder) {
        this.retryConfigurer = builder.retryConfigurer;
        this.clientConfig = builder.clientConfig;
        this.clientFactory = builder.clientFactory;
        this.sendInterceptor = builder.sendInterceptor;
        this.environment = builder.environment;
        this.mockProvideMap = builder.mockProvideMap;
        this.superObjectInterceptor = CatToosUtil.superObjectInterceptor();
    }


    public CatHttpRetryConfigurer getRetryConfigurer() {
        return retryConfigurer;
    }
    public CatClientConfiguration getClientConfig() {
        return clientConfig;
    }
    public MethodInterceptor getSuperObjectInterceptor() {
        return superObjectInterceptor;
    }
    public CatClientFactory getClientFactory() {
        return clientFactory;
    }
    public CatSendInterceptor getSendInterceptor() {
        return sendInterceptor;
    }
    public EnvironmentAdapter getEnvironment() {
        return environment;
    }
    public Class getClientMock(Class interfaceClass) {
        return mockProvideMap.get(interfaceClass);
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
        private CatClientFactory clientFactory;
        private CatSendInterceptor sendInterceptor;
        private EnvironmentAdapter environment;
        private Map<Class, Class> mockProvideMap;
        
        private CatClientMockProvide mockProvide;
        
        public Builder retryConfigurer(CatHttpRetryConfigurer retryConfigurer) {
            this.retryConfigurer = retryConfigurer;
            return this;
        }

        public Builder clientConfig(CatClientConfiguration clientConfig) {
            this.clientConfig = clientConfig;
            return this;
        }

        public Builder clientFactory(CatClientFactory clientFactory) {
            this.clientFactory = clientFactory;
            return this;
        }

        public Builder sendInterceptor(CatSendInterceptor sendInterceptor) {
            this.sendInterceptor = sendInterceptor;
            return this;
        }

        public Builder environment(Properties property) {
            this.environment = EnvironmentAdapter.environmentProperty(property);
            return this;
        }
        public Builder environment(ConfigurableListableBeanFactory configurableBeanFactory) {
            this.environment = EnvironmentAdapter.environmentProperty(configurableBeanFactory);
            return this;
        }

        public Builder mockProvide(CatClientMockProvide mockProvide) {
            this.mockProvide = mockProvide;
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

            if( sendInterceptor == null ){
                sendInterceptor = new CatSendInterceptor(){};
            }

            if( clientFactory == null ){
                clientFactory = new SimpleClientFactory();
            }
            
            if( mockProvide == null ){
                mockProvide = new CatClientMockProvide(){};
            }

            mockProvideMap = new HashMap<>();
            
            boolean enableMock = mockProvide.enableMock();
            if( enableMock ){
                Set<Class> clients = mockProvide.mockClients();
                for ( Class client : clients ) {
                    Class[] interfaces = client.getInterfaces();
                    for ( Class inter : interfaces ) {
                        mockProvideMap.put(inter, client);
                    }
                }
            }
            return new CatClientDepend(this);
        }


    }


}
