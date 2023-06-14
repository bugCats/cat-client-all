package cc.bugcat.catserver.asm;

import cc.bugcat.catface.utils.CatToosUtil;


/**
 * 方法签名处理
 * */
final class AsmMethodSignature {

    private final String name;        // 方法名
    private final Class returnType;   // 方法返回对象Type
    private final String wrapDesc;    // 包装器描述
    
    private String desc;    // 方法Type描述
    private String sign;    // 方法签名，包含返回对象、入参泛型信息

    
    AsmMethodSignature(String name, Class returnType, String wrapDesc) {
        this.name = name;
        this.returnType = returnType;
        this.wrapDesc = wrapDesc;
    }

    
    /**
     * 增强后方法返回数据类型，添加包装器类
     * @param descriptor 描述，没有泛型信息
     * @param signature 详细描述，包含泛型信息
     * */
    public void transform(Class wrap, String descriptor, String signature){

        /**
         * 包装器类存在，并且方法返回类型，不是包装器类
         * */
        if( wrapDesc != null && wrap.isAssignableFrom(returnType) == false ) {

            String[] desc = descriptor.split("\\)");
            String[] sign = (signature == null ? descriptor : signature).split("\\)");
            String returnSign = wrapDesc.replace(";", "<" + sign[1] + ">;");

            this.desc = desc[0] + ")" + wrapDesc;
            this.sign = sign[0] + ")" + returnSign;

        } else {

            this.desc = descriptor;
            this.sign = CatToosUtil.defaultIfBlank(signature, descriptor);

        }
    }

    /**
     * 如果是精简模式，收集原始参数的签名信息
     * resolverByStrategy 处理策略
     * */
    public void resolverByStrategy(ParameterResolverStrategy strategy) {
        this.desc = strategy.transformDescriptor(desc);
        this.sign = strategy.transformSignature(sign);
    }
    

    public String getName() {
        return name;
    }
    public String getDesc() {
        return desc;
    }
    public String getSign() {
        return sign;
    }
    
}
