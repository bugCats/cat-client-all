package com.bugcat.catserver.beanInfos;

import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.catserver.scanner.CatServerInitBean;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.reflect.Method;

public class CatBridgeMethodInfo {
    
    private StandardMethodMetadata metadata;
    private String sign;
    private Method method;
    private String bridgeName;
    

    
    public CatBridgeMethodInfo(StandardMethodMetadata metadata){
        this.metadata = metadata;
        this.method = metadata.getIntrospectedMethod();
        this.sign = CatToosUtil.signature(method);
        this.bridgeName = CatServerInitBean.bridgeName + method.getName();
    }
    

    public String getSign() {
        return sign;
    }
    public Method getMethod() {
        return method;
    }
    public StandardMethodMetadata getMetadata() {
        return metadata;
    }
    public String getBridgeName() {
        return bridgeName;
    }

}
