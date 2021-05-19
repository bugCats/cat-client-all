package com.bugcat.catserver.asm;

import com.bugcat.catface.spi.AbstractResponesWrapper;
import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.catserver.beanInfos.CatServerInfo;
import com.bugcat.catserver.handler.CatFaceResolverBuilder;
import com.bugcat.catserver.handler.CatMethodMapping;
import com.bugcat.catserver.utils.CatServerUtil;
import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.ClassWriter;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;
import org.springframework.cglib.core.DebuggingClassWriter;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


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

        CatAsmResult result = new CatAsmResult();

        String className = className(inter);
        ClassLoader classLoader = CatServerUtil.getClassLoader();

        InputStream stream = classLoader.getResourceAsStream(inter.getName().replace('.', '/') + ".class");
        ClassReader cr = new ClassReader(stream);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        
        Map<String, Class> returnTypeMap = new HashMap<>();
        for ( Method method : inter.getMethods() ) {
            String signature = CatMethodMapping.signature(method);
            returnTypeMap.put(signature, method.getReturnType());

            CatFaceResolverBuilder build = CatFaceResolverBuilder.builder(serverInfo.isCatface()).method(method);
            result.putResolver(signature, build);
        }
        
        CatServerClassVisitor catServer = new CatServerClassVisitor(cw, inter, serverInfo, returnTypeMap, result);
        cr.accept(catServer, ClassReader.EXPAND_FRAMES);
        
        //如果interface上没有@ResponseBody，自动添加一个
        if ( AnnotationUtils.findAnnotation(inter, ResponseBody.class) == null ) { 
            catServer.visitAnnotation(RESPONSE_BODY, true);
        }
        
        result.foreach(builder -> builder.createClass());
        
        byte[] newbs = cw.toByteArray();
        Class gen = ReflectUtils.defineClass(className, newbs, classLoader);
        printClass(gen, newbs);
        
        result.setEnhancerClass(gen);
        return result;
    }

    
    private static class CatServerClassVisitor extends ClassVisitor implements Opcodes{

        private final Map<String, Class> infoMap;
        private final Class inter;
        private final CatServerInfo serverInfo;
        private final CatAsmResult result;
        
        public CatServerClassVisitor(ClassVisitor cv, Class inter, CatServerInfo serverInfo, Map<String, Class> infoMap, CatAsmResult result) {
            super(ASM6, cv);
            this.inter = inter;
            this.serverInfo = serverInfo;
            this.infoMap = infoMap;
            this.result = result;
        }
        
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, className(inter).replace(".", "/"), signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

            String methodSign = CatMethodMapping.signature(name, descriptor);
            Class returnType = infoMap.get(methodSign);
            if( returnType != null ){

                CatMethodMapping mapping = result.getMapping();
                CatFaceResolverBuilder resolver = result.getResolver(methodSign);
                
                Signature sign = new Signature(name, returnType);
                sign.transform(serverInfo.getWarpClass(), descriptor, signature);   //处理响应结果包装器类
                sign.resolver(resolver); //处理方法入参的虚拟入参class

                MethodVisitor mv = super.visitMethod(access, sign.getName(), sign.getDesc(), sign.getSign(), exceptions);
                
                if ( resolver.hasParameter() ) {
                    mv.visitParameterAnnotation(0, REQUEST_BODY, true);
                    if( hasVaild ){
                        mv.visitParameterAnnotation(0, VALID, true);
                    }
                    mv = new CatMethodVisitor(mv);
                }
                
                mapping.interfaceToImplements(name, descriptor, sign.getDesc());
                mapping.implementsToInterface(name, sign.getDesc(), descriptor);

                return mv;
                
            } else {
                
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
        }
    }

    private static class CatMethodVisitor extends MethodVisitor {
        public CatMethodVisitor(MethodVisitor mv) {
            super(ASM6, mv);
        }
        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
            return null;
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
