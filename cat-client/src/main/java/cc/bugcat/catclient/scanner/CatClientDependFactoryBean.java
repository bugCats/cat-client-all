package cc.bugcat.catclient.scanner;

import cc.bugcat.catclient.handler.CatClientDepend;
import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.config.CatHttpRetryConfigurer;
import cc.bugcat.catclient.utils.CatClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
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
 * @author bugcat
 * */
public class CatClientDependFactoryBean extends AbstractFactoryBean<CatClientDepend> {

    public static final String BEAN_NAME = "catClientDepend";


    /**
     * CatClientDepend的依赖项
     * 必须在CatClientDepend之前加载
     * */
    @Autowired
    private CatClientUtil catClient;

    @Autowired
    private CatClientConfiguration clientConfig;

    @Autowired
    private CatHttpRetryConfigurer retryConfigurer;



    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public Class<CatClientDepend> getObjectType() {
        return CatClientDepend.class;
    }



    @Override
    protected CatClientDepend createInstance() throws Exception {
        CatClientDepend clientDepend = CatClientDepend.builder()
                .retryConfigurer(retryConfigurer)
                .clientConfig(clientConfig)
                .build();
        catClient.registerBean(this);
        return clientDepend;
    }
}
