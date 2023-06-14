package cc.bugcat.catserver.asm;


/**
 * ASM增强后的interface
 * */
final class AsmDescriptor {

    private final int access;
    private final String descriptor;
    private final String signature;
    private final String[] exceptions;

    AsmDescriptor(int access, String descriptor, String signature, String[] exceptions) {
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
