package com.bugcat.catserver.asm;

import com.bugcat.catface.annotation.Catface;
import com.bugcat.catface.spi.AbstractResponesWrapper;
import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.catserver.beanInfos.CatServerInfo;
import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.ClassWriter;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;
import org.springframework.cglib.core.DebuggingClassWriter;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.web.bind.annotation.RequestParam;
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
public final class CatAsm implements Opcodes{
    
    // 设置动态生成扩展interface的目录
    private final static String debugDir = System.getProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY);
    
    // 为interface添加@ResponseBody注解
    private final static String RESPONSE_BODY = Type.getDescriptor(ResponseBody.class);
    
    // 精简模式下添加别名
    private final static String REQUEST_PARAM = Type.getDescriptor(RequestParam.class);
    
    
    private final ClassLoader classLoader;
    
    /**
     * 类加载器
     * 必须和Spring容器的类加载器一致，否则会提示 Class Not Found
     * */
    public CatAsm(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }



    /**
     * @param inter 被@CatServer标记类的interface
     * @param serverInfo  CatServer注解类
     * */
    public Class enhancer(Class inter, CatServerInfo serverInfo) throws Exception {

        String className = className(inter);

        // 如果这个interface曾经被增强过，直接返回增强后的扩展interface
        try { return classLoader.loadClass(className); } catch ( Exception ex ) { }

        InputStream stream = classLoader.getResourceAsStream(inter.getName().replace('.', '/') + ".class");
        ClassReader cr = new ClassReader(stream);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);

        Map<String, MethodInfo> returnTypeMap = new HashMap<>();
        for ( Method method : inter.getMethods() ) {
            MethodInfo info = new MethodInfo(method);
            returnTypeMap.put(info.descriptor, info);
        }
        
        CatServerClassVisitor catServer = new CatServerClassVisitor(cw, inter, returnTypeMap, serverInfo);
        cr.accept(catServer, ClassReader.EXPAND_FRAMES);

        if ( !catServer.hasResponseBody ) { //如果interface上没有@ResponseBody，自动添加一个
            catServer.visitAnnotation(RESPONSE_BODY, true);
        }
        
        byte[] newbs = cw.toByteArray();
        Class gen = ReflectUtils.defineClass(className, newbs, classLoader);
        
        print(gen, newbs);
        
        return gen;
    }

    
    private static class CatServerClassVisitor extends ClassVisitor implements Opcodes{

        private Map<String, MethodInfo> infoMap;
        private Class inter;
        private CatServerInfo serverInfo;
        private boolean hasResponseBody = false;
        
        public CatServerClassVisitor(ClassVisitor cv, Class inter, Map<String, MethodInfo> infoMap, CatServerInfo serverInfo) {
            super(ASM4, cv);
            this.inter = inter;
            this.infoMap = infoMap;
            this.serverInfo = serverInfo;
        }
        
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, className(inter).replace(".", "/"), signature, superName, interfaces);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if( !hasResponseBody && RESPONSE_BODY.equals(desc) ){
                hasResponseBody = true;
            }
            return super.visitAnnotation(desc, visible);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = null;
            MethodInfo info = infoMap.get(descriptor);
            if( info != null ){
                Signature sign = new Signature(name, info.returnType);
                sign.transform(serverInfo.getWarpClass(), descriptor, signature);
                mv = super.visitMethod(access, sign.getName(), sign.getDesc(), sign.getSign(), exceptions);
                if( serverInfo.isCatface() ){//精简模式下，为所有入参添加别名
                    for(int idx = 0; idx < info.params.length; idx ++ ){
                        AnnotationVisitor av = mv.visitParameterAnnotation(idx, REQUEST_PARAM, true);
                        av.visit("value", "arg" + idx);
                        av.visit("required", false);
                        av.visitEnd();
                    }
                }
            } else {
                mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            }
            return mv;
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

    
    private static class MethodInfo {
        private String descriptor;
        private Class returnType;
        private Class<?>[] params;
        public MethodInfo(Method method) {
            this.descriptor = Type.getMethodDescriptor(method);
            this.returnType = method.getReturnType();
            this.params = method.getParameterTypes();
        }
    }
    
    
    private static String className(Class inter){
        return inter.getName() + CatToosUtil.bridgeName;
    }
    public static boolean isBridgeClass(Class clazz){
        return clazz != null && clazz.getSimpleName().contains(CatToosUtil.bridgeName);
    }
    
    
    private void print(Class ext, byte[] newbs){
        if( debugDir != null ){
            File dir = new File(debugDir + "/" + ext.getName().replace(".", "/") + ".class");
            if( !dir.getParentFile().exists() ){
                dir.getParentFile().mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(dir)){
                fos.write(newbs);
                fos.close();
            } catch ( Exception ex ) {
                System.out.println("CatAsm's print has an error. " + ex.getMessage());
            }
        }
    }

}
