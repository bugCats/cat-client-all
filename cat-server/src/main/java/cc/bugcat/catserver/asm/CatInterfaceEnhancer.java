package cc.bugcat.catserver.asm;

import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catserver.handler.CatServerInfo;
import cc.bugcat.catserver.utils.CatServerUtil;
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
 *
 * 被@CatServer标记类的interface，使用cglib动态生成实现类，充当Controller角色。
 *
 * 在动态代理类中，再调用被标记类的方法。
 *
 * 可以在动态代理类中，执行前后添加方法，实现自动添加包装器类。
 *
 * {@link AbstractResponesWrapper} 自动添加包装器类实现
 *
 * @author bugcat
 * */
public final class CatInterfaceEnhancer implements Opcodes {

    /**
     * 是否存在Valid验证框架
     * */
    public final static boolean HAS_VAILD = CatServerUtil.existClassAndExecute("javax.validation.Valid", clazz -> clazz != null);

    /**
     * 精简模式下
     * 为方法虚拟入参添加@Valid
     * */
    public final static String VALID = "Ljavax/validation/Valid;";

    /**
     * 精简模式下
     * 为方法虚拟入参添加@ResponseBody
     * */
    public final static String REQUEST_BODY = Type.getDescriptor(RequestBody.class);

    /**
     * 精简模式下
     * 为interface添加@ResponseBody注解
     * */
    public final static String RESPONSE_BODY = Type.getDescriptor(ResponseBody.class);
    
    /**
     * 设置动态生成扩展interface的文件路径
     * */
    private final static String debugDir = System.getProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY);



    /**
     * 增强被@CatServer标记类的 interface。
     *
     * 先遍历这些interface，解析原始的方法、入参、响应签名；
     * 再通过编辑签名，动态生成增强后interface；
     * 最后通过增强后的interface，使用cglib动态生成controller对象。
     *
     * @param interfaceClass 被@CatServer标记类的interface
     * @param serverInfo  CatServer注解类
     * @param enhancerDepend 缓存项                   
     * */
    public CatAsmInterface enhancer(Class interfaceClass, CatServerInfo serverInfo, CatEnhancerDepend enhancerDepend) throws Exception {

        ClassLoader classLoader = CatServerUtil.getClassLoader();

        CatAsmInterface result = new CatAsmInterface();

        /**
         * 多级继承情况下，优先解析最上级interface；
         * 只能单继承，不能一个interface同时继承多个interface；
         * */
        Stack<Class> interfaceParents = new Stack<>();
        Class parent = interfaceClass;
        while ( parent != null ) {
            interfaceParents.push(parent);
            Class[] interfaces = parent.getInterfaces();
            if( interfaces == null || interfaces.length == 0 ){
                parent = null;
            } else if ( interfaces.length > 1 ){
                throw new BeanCreationException(interfaceClass.getName(), "@CatServer标记的类interface只支持单继承");
            } else {
                parent = interfaces[0];
            }
        }

        // interface所有的原始方法签名信息
        final Map<String, AsmDescriptor> methodDescriptorMap = new HashMap<>();

        // interfaceClass的直接父类
        AsmInterfaceDescriptor asmDescriptor = null;
        
        //从最上级开始解析
        while ( interfaceParents.empty() == false ) {
            Class parentInterface = interfaceParents.pop();

            asmDescriptor = enhancerDepend.getClassDescriptor(parentInterface);
            if( asmDescriptor == null ){
                InputStream stream = classLoader.getResourceAsStream(toResourceName(parentInterface));
                ClassReader classReader = new ClassReader(stream);
                OnlyReaderClassVisitor onlyReader = new OnlyReaderClassVisitor(new ClassWriter(ClassWriter.COMPUTE_MAXS));
                classReader.accept(onlyReader, ClassReader.EXPAND_FRAMES);
                asmDescriptor = onlyReader.getClassDescriptor();
                enhancerDepend.putClassDescriptor(parentInterface, asmDescriptor);
            }
            
            // 子类会覆盖父类同名方法
            methodDescriptorMap.putAll(asmDescriptor.getMethodDescriptorMap());
        }

        final AsmDescriptor classDescriptor = asmDescriptor.getClassDescriptor();

        // 增强后的interface名称
        String enhancerInterfaceName = enhancerInterfaceName(interfaceClass);

        ClassWriter enhancerWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        EnhancerInterfaceClassVisitor visitor = new EnhancerInterfaceClassVisitor(enhancerWriter);
        visitor.visit(asmDescriptor.getVersion(), 
                classDescriptor.getAccess(), 
                enhancerInterfaceName.replace(".", "/"), 
                null, 
                classDescriptor.getDescriptor(), 
                new String[0]);

        // 将原interface类上的注解转移到增强类上
        Annotation[] annotations = interfaceClass.getAnnotations();
        CatServerUtil.visitAnnotation(annotations, desc -> visitor.visitAnnotation(desc, true));

        //如果interface上没有@ResponseBody，在增强类上自动添加一个
        if ( AnnotationUtils.findAnnotation(interfaceClass, ResponseBody.class) == null ) {
            visitor.visitAnnotation(RESPONSE_BODY, true);
        }

        // 对象包装器类描述
        AbstractResponesWrapper wrapperHandler = serverInfo.getWrapperHandler();
        Class wrapperClass = wrapperHandler != null ? wrapperHandler.getWrapperClass() : null;
        String wrapDesc = wrapperClass != null ? Type.getDescriptor(wrapperClass) : null;

        // interface的原始方法
        for ( Method method : interfaceClass.getMethods() ) {

            String methodName = method.getName();
            String signatureId = CatServerUtil.methodSignature(method);

            // 得到原interface的方法描述信息
            AsmDescriptor methodDescriptor = methodDescriptorMap.get(signatureId);

            /**
             * 增强方法入参处理器
             * 如果是精简模式，处理器会把方法上的入参，处理成一个虚拟对象。入参为虚拟对象的属性；
             * 如果是普通模式，则忽略
             * */
            ParameterResolverStrategy resolverStrategy = ParameterResolverStrategy.createStrategy(serverInfo.isCatface(), method);

            // 方法签名处理器
            AsmMethodSignature methodSign = new AsmMethodSignature(methodName, method.getReturnType(), wrapDesc);

            //处理响应结果包装器类
            methodSign.transform(wrapperClass, methodDescriptor.getDescriptor(), methodDescriptor.getSignature());

            //处理方法入参的虚拟入参class
            methodSign.resolverByStrategy(resolverStrategy);

            /**
             * 为增强后interface，创建方法
             * 方法名、抛出异常不变，修改了方法的响应对象、入参类型
             * */
            MethodVisitor methodVisitor = visitor.visitMethod(methodDescriptor.getAccess(), methodSign.getName(), methodSign.getDesc(), methodSign.getSign(), methodDescriptor.getExceptions());

            // 将原方法上注解，转移到增强方法上
            Annotation[] anns = method.getAnnotations();
            CatServerUtil.visitAnnotation(anns, desc -> methodVisitor.visitAnnotation(desc, true));

            if( serverInfo.isCatface() ){
                // 如果是精简模式

                if ( method.getParameterCount() > 0 ) {
                    // 原方法上存在有效入参，添加 REQUEST_BODY、VALID 注解
                    methodVisitor.visitParameterAnnotation(0, REQUEST_BODY, true);
                    
                    if( HAS_VAILD ){ //如果存在验证框架，为入参添加@Valid注解
                        methodVisitor.visitParameterAnnotation(0, VALID, true);
                    }

                    //创建虚拟入参
                    CatVirtualParameterEnhancer.generator(method, enhancerDepend, resolverStrategy); 
                }
            } else {
                // 将原方法入参上的注解，转移到增强方法、或者虚拟入参对象上

                Annotation[][] annArrs = method.getParameterAnnotations();
                if( annArrs.length > 0 ){
                    for ( int i = 0; i < annArrs.length; i ++ ) {
                        final int idx = i;
                        CatServerUtil.visitAnnotation(annArrs[i], desc -> methodVisitor.visitParameterAnnotation(idx, desc, true));
                    }
                }
            }
            methodVisitor.visitEnd();

            CatAsmMethod methodInfo = new CatAsmMethod(signatureId, CatServerUtil.methodSignature(methodName, methodSign.getDesc()), resolverStrategy.parameterResolver());
            result.putCatMethodInfo(methodInfo);
        }

        visitor.visitEnd();

        byte[] newbs = enhancerWriter.toByteArray();
        printClass(enhancerInterfaceName, newbs);

        Class gen = ReflectUtils.defineClass(enhancerInterfaceName, newbs, classLoader);
        result.setEnhancerClass(gen);
        return result;
    }


    /**
     * 收集方法签名信息
     * */
    private static class OnlyReaderClassVisitor extends ClassVisitor implements Opcodes {

        private AsmInterfaceDescriptor classDescriptor;

        public OnlyReaderClassVisitor(ClassVisitor classVisitor) {
            super(ASM5, classVisitor);
            this.classDescriptor = new AsmInterfaceDescriptor();
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            AsmDescriptor descriptor = new AsmDescriptor(access, superName, signature, interfaces);
            classDescriptor.setVersion(version);
            classDescriptor.setClassDescriptor(descriptor);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            String desc = transformReturn(descriptor);
            String sign = transformReturn(signature);
            AsmDescriptor methodDescriptor = new AsmDescriptor(ACC_PUBLIC | ACC_ABSTRACT, desc, sign, exceptions);
            classDescriptor.putMethodDescriptor(CatServerUtil.methodSignature(name, desc), methodDescriptor);
            return null;
        }

        public AsmInterfaceDescriptor getClassDescriptor() {
            return classDescriptor;
        }
    }



    /**
     * 增强interface ClassVisitor
     * */
    private static class EnhancerInterfaceClassVisitor extends ClassVisitor implements Opcodes{
        public EnhancerInterfaceClassVisitor(ClassVisitor cv) {
            super(ASM5, cv);
        }
    }




    /**
     * 如果方法返回参数如果基础数据类型，转换成对应包装类。
     * */
    public static String transformReturn(String desc){
        if( desc == null ){
            return null;
        }
        int index = desc.indexOf(")") + 1;
        String argsDesc = desc.substring(0, index);
        String returnDesc = desc.substring(index);
        return argsDesc + CatTypeTools.getReturnType(returnDesc);
    }

    /**
     * 判断是否为增强后对象
     * */
    public static boolean isBridgeClass(Class clazz){
        return clazz != null && clazz.getSimpleName().contains(CatServerUtil.BRIDGE_NAME);
    }

    /**
     * 增强后简称
     * */
    public static String bridgeClassSimpleNam(Class clazz){
        return clazz.getSimpleName() + CatServerUtil.BRIDGE_NAME;
    }
    
    /**
     * 生成增强后全称
     * */
    public static String enhancerInterfaceName(Class inter){
        return inter.getName() + CatServerUtil.BRIDGE_NAME;
    }
    

    /**
     * class 转资源路径
     * */
    public static String toResourceName(Class clazz){
        return clazz.getName().replace('.', '/') + ".class";
    }


    /**
     * 打印动态生成的interface
     * */
    public static void printClass(String enhancerClass, byte[] newbs){
        if( debugDir != null ){
            File dir = new File(debugDir + "/" + enhancerClass.replace(".", "/") + ".class");
            if( !dir.getParentFile().exists() ){
                dir.getParentFile().mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(dir)){
                fos.write(newbs);
            } catch ( Exception ex ) {
                System.err.println("CatInterfaceEnhancer's print has an error. " + ex.getMessage());
            }
        }
    }

}
