package com.bugcat.catserver.asm;

import com.bugcat.catserver.scanner.CatServerInitBean;
import org.springframework.asm.*;
import org.springframework.cglib.core.ReflectUtils;

import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CatAsm implements Opcodes {
    
    private ClassLoader classLoader;


    public CatAsm(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * @param inter 被@CatServer标记类的interface
     * @param warp  统一响应类
     */
    public Class enhancer(Class inter, Class warp) throws Exception {

        Map<String, Method> methodMap = new HashMap<>();
        Method[] methods = inter.getMethods();
        for ( Method method : methods ) {
            methodMap.put(method.getName(), method);
        }
        
        ClassReader cr = new ClassReader(inter.getName());
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        
        
        CatServerClassVisitor catServer = new CatServerClassVisitor(cw, inter, methodMap, warp);
        cr.accept(catServer, ClassReader.EXPAND_FRAMES);

        byte[] newbs = cw.toByteArray();

//        FileOutputStream fos = new FileOutputStream("F:/tmp/" + inter.getSimpleName() + ".class");//覆盖当前class文件
//        fos.write(newbs);
//        fos.close();

        Class gen = ReflectUtils.defineClass(className(inter), newbs, classLoader);
        
        return gen;
    }


    private static class CatServerClassVisitor extends ClassVisitor implements Opcodes {

        private Class inter;
        private Map<String, Method> methodMap;
        private Class warp;
        
        public CatServerClassVisitor(ClassVisitor cv, Class inter, Map<String, Method> methodMap, Class warp) {
            super(ASM4, cv);
            this.inter = inter;
            this.methodMap = methodMap;
            this.warp = warp;
        }
        
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, className(inter).replace(".", "/"), signature, superName, new String[]{inter.getName().replace(".", "/")});
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = null;
            Method method = methodMap.get(name);
            if( method != null ){
                Signature sign = new Signature(CatServerInitBean.bridgeName + method.getName(), method);
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
        
        private Method method;
        
        public Signature(String name, Method method) {
            this.name = name;
            this.method = method;
        }
        
        public void transform(Class warp, String descriptor, String signature){
            
            if( warp != null && !warp.isAssignableFrom(method.getReturnType()) ) {
                
                // warp描述
                String warpDesc = Type.getDescriptor(warp);

                String[] sign = (signature == null ? descriptor : signature).split("\\)");
                String returnDesc = warpDesc.replace(";", "<" + sign[1] + ">;");

                this.desc = sign[0] + ")" + warpDesc;
                this.sign = sign[0] + ")" + returnDesc;
                
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

    
    
    private final static String className(Class inter){
        return (inter.getName() + CatServerInitBean.bridgeName);
    }
}
