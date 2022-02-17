package cc.bugcat.catserver.asm;

import cc.bugcat.catserver.beanInfos.CatMethodInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * 增强被@CatServer标记的interface处理结果
 * */
public class CatAsmResult {


    //增强后的class
    private Class enhancerClass;

    //增强前后的方法信息
    private Map<String, CatMethodInfo> methodInfoMap = new HashMap<>();


    public Class getEnhancerClass() {
        return enhancerClass;
    }
    public void setEnhancerClass(Class enhancerClass) {
        this.enhancerClass = enhancerClass;
    }

    public Map<String, CatMethodInfo> getMethodInfoMap() {
        return methodInfoMap;
    }
    public void putCatMethodInfo(CatMethodInfo methodInfo) {
        methodInfoMap.put(methodInfo.getEnhancerSignatureId(), methodInfo);
    }

}
