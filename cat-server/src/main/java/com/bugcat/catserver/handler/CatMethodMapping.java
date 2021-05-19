package com.bugcat.catserver.handler;

import org.springframework.asm.Type;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


public class CatMethodMapping {

    
    private Map<String, String> map = new HashMap<>();
    
    
    // interface方法到实现类方法映射
    public void interfaceToImplements(String methodName, String interfaceDesc, String implementsDesc){
        this.map.put("sd_" + methodName + "@" + interfaceDesc, methodName + "@" + implementsDesc);
    }
    
    
    // 实现类方法到interface方法映射
    public void implementsToInterface(String methodName, String implementsDesc, String interfaceDesc){
        this.map.put("ds_" + methodName + "@" + implementsDesc, methodName + "@" + interfaceDesc);
    }
    
    
    
    
    /**
     * 获取实现类方法签名
     * */
    public String getImplementsSign(Method method){
        return this.map.get("sd_" + signature(method));
    }
    
    /**
     * 获取interface方法签名
     * */
    public String getInterfaceSign(Method method){
        return this.map.get("ds_" + signature(method));
    }
    
 
    public void putAll(CatMethodMapping other){
        this.map.putAll(other.map);
    }
    
    
    
    
    public static String signature(Method method){
        return method.getName() + "@" + Type.getMethodDescriptor(method);
    }
    public static String signature(String methodName, String descriptor){
        return methodName + "@" + descriptor;
    }

    
}
