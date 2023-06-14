package cc.bugcat.catserver.asm;

import cc.bugcat.catserver.spi.CatParameterResolver;

/**
 * 增强后interface的method描述信息
 *
 * @author bugcat
 * */
public class CatAsmMethod {


    /**
     * 增强前方法签名
     * */
    private final String interfaceSignatureId;


    /**
     * 增强后方法签名
     * */
    private final String enhancerSignatureId;

    /**
     * 方法上的虚拟入参处理策略
     * */
    private final CatParameterResolver parameterResolver;

    
    public CatAsmMethod(String interfaceSignatureId, String enhancerSignatureId, CatParameterResolver parameterResolver) {
        this.interfaceSignatureId = interfaceSignatureId;
        this.enhancerSignatureId = enhancerSignatureId;
        this.parameterResolver = parameterResolver;
    }

    public String getInterfaceSignatureId() {
        return interfaceSignatureId;
    }
    public String getEnhancerSignatureId() {
        return enhancerSignatureId;
    }
    public CatParameterResolver getParameterResolver() {
        return parameterResolver;
    }
}
