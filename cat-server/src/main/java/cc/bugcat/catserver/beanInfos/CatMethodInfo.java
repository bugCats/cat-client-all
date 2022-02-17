package cc.bugcat.catserver.beanInfos;

import cc.bugcat.catserver.handler.CatArgumentResolverStrategy;

/**
 * 增强后interface的方法描述
 * */
public class CatMethodInfo {

    // 增强前方法签名
    private String interfaceSignatureId;

    // 增强后方法签名
    private String enhancerSignatureId;

    // 方法上的虚拟入参处理策略
    private CatArgumentResolverStrategy resolverStrategy;



    public String getInterfaceSignatureId() {
        return interfaceSignatureId;
    }
    public void setInterfaceSignatureId(String interfaceSignatureId) {
        this.interfaceSignatureId = interfaceSignatureId;
    }

    public String getEnhancerSignatureId() {
        return enhancerSignatureId;
    }
    public void setEnhancerSignatureId(String enhancerSignatureId) {
        this.enhancerSignatureId = enhancerSignatureId;
    }

    public CatArgumentResolverStrategy getResolverStrategy() {
        return resolverStrategy;
    }
    public void setResolverStrategy(CatArgumentResolverStrategy resolverStrategy) {
        this.resolverStrategy = resolverStrategy;
    }
}
