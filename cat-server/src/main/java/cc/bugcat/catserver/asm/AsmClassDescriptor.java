package cc.bugcat.catserver.asm;

import java.util.HashMap;
import java.util.Map;

final class AsmClassDescriptor {


    /**
     * jdk版本
     * */
    private int version;
    
    /**
     * 类描述信息
     * */
    private AsmDescriptor classDescriptor;
    
    /**
     * 方法描述
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
    public void setClassDescriptor(int access, String descriptor, String signature, String[] exceptions){
        this.classDescriptor = new AsmDescriptor(access, descriptor, signature, exceptions);
    }
    public AsmDescriptor getClassDescriptor() {
        return classDescriptor;
    }
    
    
    /**
     * 设置方法描述信息
     * */
    public void putMethodDescriptor(String signatureId, int access, String descriptor, String signature, String[] exceptions){
        AsmDescriptor methodDescriptor = new AsmDescriptor(access, descriptor, signature, exceptions);
        this.methodDescriptorMap.put(signatureId, methodDescriptor);
    }
    public Map<String, AsmDescriptor> getMethodDescriptorMap() {
        return methodDescriptorMap;
    }


    /**
     * 类、方法签名信息
     * */
    static class AsmDescriptor {

        private final int access;
        private final String descriptor;
        private final String signature;
        private final String[] exceptions;

        public AsmDescriptor(int access, String descriptor, String signature, String[] exceptions) {
            this.access = access;
            this.descriptor = descriptor;
            this.signature = signature;
            this.exceptions = exceptions;
        }

        public int getAccess() {
            return access;
        }

        public String getDescriptor() {
            return descriptor;
        }

        public String getSignature() {
            return signature;
        }

        public String[] getExceptions() {
            return exceptions;
        }
    }

}
