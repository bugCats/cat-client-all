package cc.bugcat.catserver.asm;

import java.util.HashMap;
import java.util.Map;

/**
 * ASM增强后的interface
 * */
final class AsmInterfaceDescriptor {


    /**
     * jdk版本
     * */
    private int version;
    
    /**
     * asm-interface 描述信息
     * */
    private AsmDescriptor classDescriptor;
    
    /**
     * asm-method 描述
     * */
    private Map<String, AsmDescriptor> methodDescriptorMap = new HashMap<>(64);

    
    
    
    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }

    
    /**
     * 设置类描述信息
     * */
    public void setClassDescriptor(AsmDescriptor classDescriptor){
        this.classDescriptor = classDescriptor;
    }
    public AsmDescriptor getClassDescriptor() {
        return classDescriptor;
    }
    
    
    /**
     * 设置方法描述信息
     * */
    public void putMethodDescriptor(String signatureId, AsmDescriptor methodDescriptor){
        this.methodDescriptorMap.put(signatureId, methodDescriptor);
    }
    
    public Map<String, AsmDescriptor> getMethodDescriptorMap() {
        return methodDescriptorMap;
    }


}
