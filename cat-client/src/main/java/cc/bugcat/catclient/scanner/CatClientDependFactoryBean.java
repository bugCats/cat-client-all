package cc.bugcat.catclient.scanner;

import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.config.CatHttpRetryConfigurer;
import cc.bugcat.catclient.handler.CatClientDepend;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatSendInterceptor;
import cc.bugcat.catclient.utils.CatClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.List;

/**
 * 客户端前置依赖项管理。
 *
 * 在构建客户端时，动态从ApplicationContext容器中获取依赖组件，这样会导致有些情况，客户端比依赖组件更先加载，最终使用了默认值的问题。
 * 
 * 因此，所有的客户端前置仅依赖CatClientDepend，而使用CatClientDependFactoryBean来创建CatClientDepend，
 * 把真实的所有前置依赖，在CatClientDependFactoryBean预加载：
 *      加载顺序：所有依赖组件 > CatClientDependFactoryBean > CatClientDepend > 客户端
 * 
 * 
 * 里面属性不能删除！否则不能保证客户端的依赖关系！
 *
 *
 * @author bugcat
 * */
public class CatClientDependFactoryBean extends AbstractFactoryBean<CatClientDepend> {

    public static final String BEAN_NAME = "catClientDepend";

    @Autowired
    private CatClientUtil catClient;

    @Autowired
    private CatClientConfiguration clientConfig;

    @Autowired
    private CatHttpRetryConfigurer retryConfigurer;

    @Autowired(required = false)
    private List<CatClientFactory> clientFactories;

    @Autowired(required = false)
    private List<CatSendInterceptor> sendInterceptors;
    
    
    @Autowired
    private ConfigurableListableBeanFactory configurableBeanFactory;
    
    
    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public Class<CatClientDepend> getObjectType() {
        return CatClientDepend.class;
    }


    @Override
    protected CatClientDepend createInstance() {
        CatClientDepend clientDepend = CatClientDepend.builder()
                .retryConfigurer(retryConfigurer)
                .clientConfig(clientConfig)
                .environment(configurableBeanFactory)
                .build();
        catClient.registerBean(this.getClass(), this);
        return clientDepend;
    }
}
