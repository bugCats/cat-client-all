package cc.bugcat.catserver.asm;

import cc.bugcat.catserver.handler.CatParameterResolverStrategy;

/**
 * 增强后interface的method描述信息
 *
 * @author bugcat
 * */
public class CatAsmMethod {


    /**
     * 增强前方法签名
     * */
    private String interfaceSignatureId;


    /**
     * 增强后方法签名
     * */
    private String enhancerSignatureId;


    /**
     * 方法上的虚拟入参处理策略
     * */
    private CatParameterResolverStrategy resolverStrategy;




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

    public CatParameterResolverStrategy getResolverStrategy() {
        return resolverStrategy;
    }
    public void setResolverStrategy(CatParameterResolverStrategy resolverStrategy) {
        this.resolverStrategy = resolverStrategy;
    }

}
