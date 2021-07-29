package com.bugcat.catserver.asm;

import com.bugcat.catface.spi.AbstractResponesWrapper;
import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.catserver.beanInfos.CatServerInfo;
import com.bugcat.catserver.handler.CatFaceResolverBuilder;
import com.bugcat.catserver.handler.CatMethodMapping;
import com.bugcat.catserver.utils.CatServerUtil;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.ClassWriter;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.cglib.core.DebuggingClassWriter;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


/**
 * 
 * {@link AbstractResponesWrapper} 自动添加包装器类实现
 * 
 * 被@CatServer标记类的interface，使用cglib代理，动态生成类充当Controller角色
 * 
 * 在动态代理类中，再使用反射调用被标记类的方法
 * 
 * 可以在动态代理类中，执行前后添加方法，实现自动添加包装器类
 * 
 * */
public final class CatInterfaceEnhancer implements Opcodes{

    public final static boolean hasVaild;
    static {
        boolean exist = false;
        try {
            Class.forName("javax.validation.Valid");
            exist = true;
        } catch ( Exception e ) {
            exist = false;
        }
        hasVaild = exist;
    }
    

    // 设置动态生成扩展interface的目录
    private final static String debugDir = System.getProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY);

    
    // 为interface添加@ResponseBody注解
    public final static String RESPONSE_BODY = Type.getDescriptor(ResponseBody.class);

    // 为方法虚拟入参添加@ResponseBody
    public final static String REQUEST_BODY = Type.getDescriptor(RequestBody.class);
    // 为方法虚拟入参添加@Valid
    public final static String VALID = "Ljavax/validation/Valid;";
    
    
    /**
     * @param inter 被@CatServer标记类的interface
     * @param serverInfo  CatServer注解类
     * */
    public CatAsmResult enhancer(Class inter, CatServerInfo serverInfo) throws Exception {

        ClassLoader classLoader = CatServerUtil.getClassLoader();

        CatAsmResult result = new CatAsmResult();

        Stack<Class> interParents = new Stack<>();
        Class parent = inter;
        while ( parent != null ) {
            interParents.push(parent);
            Class[] interfaces = parent.getInterfaces();
            if( interfaces == null || interfaces.length == 0 ){
                parent = null;
            } else if ( interfaces.length > 1 ){
                throw new BeanCreationException(inter.getName(), "cat-client interface只支持单继承");
            } else {
                parent = interfaces[0];
            }
        }

        Map<String, AsmClassMethodDescriptor> methodDescriptorMap = new HashMap<>();
        OnlyReaderClassVisitor onlyReader = null;
        while ( !interParents.empty() ) {
            Class parentInter = interParents.pop();
            InputStream stream = classLoader.getResourceAsStream(parentInter.getName().replace('.', '/') + ".class");
            ClassReader cr = new ClassReader(stream);
            onlyReader = new OnlyReaderClassVisitor(new ClassWriter(ClassWriter.COMPUTE_MAXS));
            cr.accept(onlyReader, ClassReader.EXPAND_FRAMES);
            methodDescriptorMap.putAll(onlyReader.methodDescriptorMap);
        }
        onlyReader.methodDescriptorMap = methodDescriptorMap;
        AsmClassMethodDescriptor classDescriptor = onlyReader.classDescriptor;

        String className = className(inter);
        ClassWriter enhancerWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        CatServerClassVisitor catServer = new CatServerClassVisitor(enhancerWriter);
        catServer.visit(onlyReader.version, classDescriptor.access, className.replace(".", "/"), null, classDescriptor.descriptor, new String[0]);
        
        //如果interface上没有@ResponseBody，自动添加一个
        if ( AnnotationUtils.findAnnotation(inter, ResponseBody.class) == null ) {
            catServer.visitAnnotation(RESPONSE_BODY, true);
        }
        Annotation[] annotations = inter.getAnnotations();
        CatServerUtil.visitAnnotation(annotations, desc -> catServer.visitAnnotation(desc, true));
        
        for ( Method method : inter.getMethods() ) {
            String methodMap = method.getName();
            
            CatMethodMapping mapping = result.getMapping();
            CatFaceResolverBuilder resolver = CatFaceResolverBuilder.builder(serverInfo.isCatface()).method(method);

            Class returnType = method.getReturnType();
            Signature sign = new Signature(methodMap, returnType);
            
            AsmClassMethodDescriptor methodDescriptor = methodDescriptorMap.get(methodMap + "@" + Type.getMethodDescriptor(method));
            sign.transform(serverInfo.getWarpClass(), methodDescriptor.descriptor, methodDescriptor.signature);   //处理响应结果包装器类
            sign.resolver(resolver); //处理方法入参的虚拟入参class

            MethodVisitor visitor = catServer.visitMethod(methodDescriptor.access, sign.getName(), sign.getDesc(), sign.getSign(), methodDescriptor.exceptions);
            
            Annotation[] anns = method.getAnnotations();
            CatServerUtil.visitAnnotation(anns, desc -> visitor.visitAnnotation(desc, true));
        
            if( serverInfo.isCatface() ){
                if ( resolver.hasParameter() ) {
                    visitor.visitParameterAnnotation(0, REQUEST_BODY, true);
                    if( hasVaild ){
                        visitor.visitParameterAnnotation(0, VALID, true);
                    }
                }
            } else {
                Annotation[][] annArrs = method.getParameterAnnotations();
                if( annArrs.length > 0 ){
                    for ( int i = 0; i < annArrs.length; i ++ ) {
                        final int idx = i;
                        CatServerUtil.visitAnnotation(annArrs[i], desc -> visitor.visitParameterAnnotation(idx, desc, true));
                    }
                }
            }
            visitor.visitEnd();

            mapping.putInterfaceToImplements(methodMap, methodDescriptor.descriptor, sign.getDesc());
            mapping.putImplementsToInterface(methodMap, sign.getDesc(), methodDescriptor.descriptor);

            resolver.createClass(); //创建虚拟入参
            result.putResolver(CatMethodMapping.uuid(method), resolver);
        }
        
        catServer.visitEnd();

        byte[] newbs = enhancerWriter.toByteArray();
        Class gen = ReflectUtils.defineClass(className, newbs, classLoader);
        printClass(gen, newbs);

        result.setEnhancerClass(gen);
        return result;
    }

    
    
    private static class OnlyReaderClassVisitor extends ClassVisitor implements Opcodes{
        private int version;
        private AsmClassMethodDescriptor classDescriptor;
        private Map<String, AsmClassMethodDescriptor> methodDescriptorMap = new HashMap<>();
        
        public OnlyReaderClassVisitor(ClassVisitor classVisitor) {
            super(ASM6, classVisitor);
        }
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.version = version;
            this.classDescriptor = new AsmClassMethodDescriptor(access, name, superName, signature, interfaces);
        }

        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            AsmClassMethodDescriptor methodDesc = new AsmClassMethodDescriptor(access, name, descriptor, signature, exceptions);
            this.methodDescriptorMap.put(name+"@" + descriptor, methodDesc);
            return null;
        }
    }
    
    
    private static class AsmClassMethodDescriptor {
        private final int access;
        private final String name;
        private final String descriptor;
        private final String signature;
        private final String[] exceptions;
        public AsmClassMethodDescriptor(int access, String name, String descriptor, String signature, String[] exceptions) {
            this.access = access;
            this.name = name;
            this.descriptor = descriptor;
            this.signature = signature;
            this.exceptions = exceptions;
        }
    }
    
    
    
    private static class CatServerClassVisitor extends ClassVisitor implements Opcodes{
        public CatServerClassVisitor(ClassVisitor cv) {
            super(ASM6, cv);
        }
    }
    
    

    
    
    private static class Signature {
        
        private String name;
        
        private String desc;
        private String sign;
        
        private Class returnType;
        
        public Signature(String name, Class returnType) {
            this.name = name;
            this.returnType = returnType;
        }
        
        public void transform(Class warp, String descriptor, String signature){
            
            // 包装器类存在，并且方法返回类型，不是包装器类
            if( warp != null && !warp.isAssignableFrom(returnType) ) {

                // warp描述
                String warpDesc = Type.getDescriptor(warp);

                String[] desc = descriptor.split("\\)");
                String[] sign = (signature == null ? descriptor : signature).split("\\)");
                String returnSign = warpDesc.replace(";", "<" + sign[1] + ">;");
                
                this.desc = desc[0] + ")" + warpDesc;
                this.sign = sign[0] + ")" + returnSign;
                
            } else {
                this.desc = descriptor;
                this.sign = signature;
            }
        }
        
        // 如果是精简模式，收集原始参数的签名信息
        public void resolver(CatFaceResolverBuilder resolver) {
            this.desc = resolver.descriptor(desc);
            this.sign = resolver.signature(sign);
        }

        
        public String getName() {
            return name;
        }
        public String getDesc() {
            return desc;
        }
        public String getSign() {
            return sign;
        }

    }

    
    
    private static String className(Class inter){
        return inter.getName() + CatToosUtil.bridgeName;
    }
    public static boolean isBridgeClass(Class clazz){
        return clazz != null && clazz.getSimpleName().contains(CatToosUtil.bridgeName);
    }
    
    
    
    public static void printClass(Class ext, byte[] newbs){
        if( debugDir != null ){
            File dir = new File(debugDir + "/" + ext.getName().replace(".", "/") + ".class");
            if( !dir.getParentFile().exists() ){
                dir.getParentFile().mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(dir)){
                fos.write(newbs);
                fos.close();
            } catch ( Exception ex ) {
                System.out.println("CatInterfaceEnhancer's print has an error. " + ex.getMessage());
            }
        }
    }
    
}
