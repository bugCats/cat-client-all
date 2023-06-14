package cc.bugcat.catserver.asm;

import java.util.HashMap;
import java.util.Map;

/**
 * 被@CatServer标记的interface增强后的类描述
 *
 * @author bugcat
 * */
public class CatAsmInterface {


    /**
     * 增强后的class
     * */
    private Class enhancerClass;


    /**
     * 增强后的方法信息；
     * 增强后签名id：方法描述信息
     * */
    private Map<String, CatAsmMethod> methodInfoMap = new HashMap<>();



    public Class getEnhancerClass() {
        return enhancerClass;
    }
    public void setEnhancerClass(Class enhancerClass) {
        this.enhancerClass = enhancerClass;
    }

    public Map<String, CatAsmMethod> getMethodInfoMap() {
        return methodInfoMap;
    }
    public void putCatMethodInfo(CatAsmMethod methodInfo) {
        methodInfoMap.put(methodInfo.getEnhancerSignatureId(), methodInfo);
    }

}
