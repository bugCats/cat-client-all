package com.bugcat.catserver.asm;

import com.bugcat.catface.spi.ResponesWrapper;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * {@link ResponesWrapper} 自动添加包装器类实现
 * 
 * interface中的原始方法，均是返回真正的业务响应
 * 要实现自动添加包装器类，可以为原始方法，用asm动态添加桥接方法。
 * 
 * 使用桥接方法调用原始返回，再把业务响应进行包装。
 * 
 * 
 * 由于原始方法上有注解，使用asm动态生成的方法，会丢失这些注解，
 * 最终导致Controller框架中的RequestMapping解析入参异常，swagger框架也会失效
 * 
 * 
 * 此处做法为：先使用asm动态生成一个扩展interface，继承原始interface。
 * 再解析原始interface、原始方法，修改方法名、响应类型，变成扩展interface的桥接方法；
 * 这样，扩展interface中的桥接方法，就会保留注解信息
 * 
 * 最后使用cglib动态生成类时，采用扩展interface
 * */
public final class CatAsm implements Opcodes{
    
    
    // 设置动态生成扩展interface的目录
    private final static String debugDir = System.getProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY);
    
    private final static String RESPONSE_BODY = "Lorg/springframework/web/bind/annotation/ResponseBody;";
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
     * @param warp  统一响应类
     * */
    public Class enhancer(Class inter, Class warp) throws Exception {

        String className = className(inter);

        // 如果这个interface曾经被增强过，直接返回增强后的扩展interface
        try { return classLoader.loadClass(className); } catch ( Exception ex ) { }

        InputStream stream = classLoader.getResourceAsStream(inter.getName().replace('.', '/') + ".class");
        ClassReader cr = new ClassReader(stream);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);

        Map<String, Class> returnTypeMap = new HashMap<>();
        Method[] methods = inter.getMethods();
        for ( Method method : methods ) {
            returnTypeMap.put(Type.getMethodDescriptor(method), method.getReturnType());
        }
        
        CatServerClassVisitor catServer = new CatServerClassVisitor(cw, inter, returnTypeMap, warp);
        cr.accept(catServer, ClassReader.EXPAND_FRAMES);

        if ( !catServer.hasResponseBody ) {
            catServer.visitAnnotation(RESPONSE_BODY, true);
        }
        
        byte[] newbs = cw.toByteArray();
        Class gen = ReflectUtils.defineClass(className, newbs, classLoader);
        
        print(gen, newbs);
        
        return gen;
    }


    
    private static class CatServerClassVisitor extends ClassVisitor implements Opcodes{

        private Map<String, Class> returnTypeMap;
        private Class inter;
        private Class warp;
        private boolean hasResponseBody = false;
        
        public CatServerClassVisitor(ClassVisitor cv, Class inter, Map<String, Class> returnTypeMap, Class warp) {
            super(ASM4, cv);
            this.inter = inter;
            this.returnTypeMap = returnTypeMap;
            this.warp = warp;
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
            Class returnType = returnTypeMap.get(descriptor);
            if( returnType != null ){
                Signature sign = new Signature(name, returnType);
                sign.transform(warp, descriptor, signature);
                mv = super.visitMethod(access, sign.getName(), sign.getDesc(), sign.getSign(), exceptions);
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

    
    
    private static String className(Class inter){
        return inter.getName() + CatServerUtil.bridgeName;
    }
    public static boolean isBridgeClass(Class clazz){
        return clazz != null && clazz.getSimpleName().contains(CatServerUtil.bridgeName);
    }
    public static String trimClassName(String className){
        return className.contains(CatServerUtil.bridgeName) ? className.substring(0, className.length() - CatServerUtil.bridgeName.length()) : className;
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
