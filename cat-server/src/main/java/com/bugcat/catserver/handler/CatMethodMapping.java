package com.bugcat.catserver.handler;

import com.bugcat.catserver.asm.CatInterfaceEnhancer;
import org.springframework.asm.Type;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


public class CatMethodMapping {

    
    private Map<String, String> map = new HashMap<>();
    
    
    // interface方法到实现类方法映射
    public void putInterfaceToImplements(String methodName, String interfaceDesc, String implementsDesc){
        this.map.put("sd_" + methodName + "@" + interfaceDesc, methodName + "@" + implementsDesc);
    }
    
    
    // 实现类方法到interface方法映射
    public void putImplementsToInterface(String methodName, String implementsDesc, String interfaceDesc){
        this.map.put("ds_" + methodName + "@" + implementsDesc, methodName + "@" + interfaceDesc);
    }
    
    
    
    
    /**
     * 获取实现类方法签名
     * */
    public String getImplementsUuid(Method method){
        return this.map.get("sd_" + uuid(method));
    }
    
    /**
     * 获取interface方法签名
     * */
    public String getInterfaceUuid(Method method){
        return this.map.get("ds_" + uuid(method));
    }
    
 
    public void putAll(CatMethodMapping other){
        this.map.putAll(other.map);
    }
    
    
    
    
    public static String uuid(Method method){
        return method.getName() + "@" + CatInterfaceEnhancer.transformReturn(Type.getMethodDescriptor(method));
    }
    public static String uuid(String methodName, String descriptor){
        return methodName + "@" + descriptor;
    }

    
}
