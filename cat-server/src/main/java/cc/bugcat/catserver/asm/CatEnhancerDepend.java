package cc.bugcat.catserver.asm;

import cc.bugcat.catface.handler.EnvironmentAdapter;
import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.config.CatServerConfiguration;
import cc.bugcat.catserver.handler.CatMethodAopInterceptor;
import cc.bugcat.catserver.spi.CatVirtualProprietyPolicy;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * asm相关缓存
 * */
public class CatEnhancerDepend {

    /**
     * object默认方法拦截器
     * */
    private final MethodInterceptor objectMethodInterceptor;
    
    /**
     * getter、setter方法生成规则
     * */
    private final CatVirtualProprietyPolicy proprietyPolicy;
    

    /**
     * interface增强后结果缓存，防止同一个interface被反复增强
     * */
    private final Map<Class, CatAsmInterface> ctrlAsmMap;

    /**
     * interface类解析后的信息
     * */
    private final Map<Class, AsmInterfaceDescriptor> classDescMap;
    
    
    public CatEnhancerDepend(int serverSize) {
        this.objectMethodInterceptor = CatToosUtil.defaultMethodInterceptor();
        this.ctrlAsmMap = new HashMap<>(serverSize * 2);
        this.classDescMap = new HashMap<>(serverSize * 4);
        this.proprietyPolicy = CatVirtualProprietyPolicy.loadService();
    }

    public void clear(){
        ctrlAsmMap.clear();
        classDescMap.clear();
    }


    public MethodInterceptor getObjectMethodInterceptor() {
        return objectMethodInterceptor;
    }
    public CatVirtualProprietyPolicy getProprietyPolicy() {
        return proprietyPolicy;
    }

    public CatAsmInterface getControllerDescriptor(Class interfaceClass) {
        return ctrlAsmMap.get(interfaceClass);
    }
    public void putControllerDescriptor(Class interfaceClass, CatAsmInterface asmResult) {
        ctrlAsmMap.put(interfaceClass, asmResult);
    }

    public AsmInterfaceDescriptor getClassDescriptor(Class interfaceClass) {
        return classDescMap.get(interfaceClass);
    }
    public void putClassDescriptor(Class interfaceClass, AsmInterfaceDescriptor classDescriptor) {
        classDescMap.put(interfaceClass, classDescriptor);
    }

}
