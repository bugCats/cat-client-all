package com.bugcat.catserver.asm;

import com.bugcat.catserver.handler.CatFaceResolverBuilder;
import com.bugcat.catserver.handler.CatMethodMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 增强被@CatServer标记的interface处理结果
 * 
 * */
public class CatAsmResult {

    
    //增强后的class
    private Class enhancerClass;

    
    //增强后的class与原class method映射关系
    private CatMethodMapping mapping = new CatMethodMapping();

    
    //方法上的虚拟入参
    private Map<String, CatFaceResolverBuilder> resolverMap = new HashMap<>();   

    
    
    public void putResolver(String signature, CatFaceResolverBuilder build){
        resolverMap.put(signature, build);
    }
    public CatFaceResolverBuilder getResolver(String signature){
        return resolverMap.get(signature);
    }
    
    public Map<String, CatFaceResolverBuilder> getResolverMap() {
        return resolverMap;
    }
    public void foreach(Consumer<CatFaceResolverBuilder> consumer){
        resolverMap.forEach((key, value) -> consumer.accept(value));
    }
    
    
    
    public Class getEnhancerClass() {
        return enhancerClass;
    }
    public void setEnhancerClass(Class enhancerClass) {
        this.enhancerClass = enhancerClass;
    }

    public CatMethodMapping getMapping() {
        return mapping;
    }


}
