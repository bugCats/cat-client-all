package cc.bugcat.catserver.asm;

import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catserver.beanInfos.CatMethodInfo;
import cc.bugcat.catserver.beanInfos.CatServerInfo;
import cc.bugcat.catserver.handler.CatArgumentResolverStrategy;
import cc.bugcat.catserver.utils.CatServerUtil;
import org.springframework.asm.*;
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
 * 在动态代理类中，再调用被标记类的方法
 *
 * 可以在动态代理类中，执行前后添加方法，实现自动添加包装器类
 *
 * */
public final class CatInterfaceEnhancer implements Opcodes{

    /**
     * 是否存在Valid验证框架
     * */
    public final static boolean HAS_VAILD = CatServerUtil.existClassAndExecute("javax.validation.Valid", clazz -> {});

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
     * 设置动态生成扩展interface的目录
     * */
    private final static String debugDir = System.getProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY);


    /**
     * 增强被@CatServer标记类的 interface
     *
     * 先遍历这些interface，解析原始的方法、入参、响应签名
     * 再通过编辑签名，动态生成增强后interface
     * 最后通过增强后的interface，使用cglib动态生成controller对象
     *
     * @param interfaceClass 被@CatServer标记类的interface
     * @param serverInfo  CatServer注解类
     * */
    public CatAsmResult enhancer(Class interfaceClass, CatServerInfo serverInfo) throws Exception {

        ClassLoader classLoader = CatServerUtil.getClassLoader();

        CatAsmResult result = new CatAsmResult();

        /**
         * 多级继承情况下，优先解析最上级interface
         * 只能单继承，不能一个interface同时继承多个interface
         * */
        Stack<Class> interfaceParents = new Stack<>();
        Class parent = interfaceClass;
        while ( parent != null ) {
            interfaceParents.push(parent);
            Class[] interfaces = parent.getInterfaces();
            if( interfaces == null || interfaces.length == 0 ){
                parent = null;
            } else if ( interfaces.length > 1 ){
                throw new BeanCreationException(interfaceClass.getName(), "cat-client interface只支持单继承");
            } else {
                parent = interfaces[0];
            }
        }

        // 所有的方法签名信息
        final Map<String, AsmClassMethodDescriptor> methodDescriptorMap = new HashMap<>();

        //最后一个直接父类interface
        OnlyReaderClassVisitor onlyReader = null;
        while ( !interfaceParents.empty() ) {
            Class parentInterface = interfaceParents.pop();

            InputStream stream = classLoader.getResourceAsStream(toResourceName(parentInterface));
            ClassReader classReader = new ClassReader(stream);
            onlyReader = new OnlyReaderClassVisitor(new ClassWriter(ClassWriter.COMPUTE_MAXS));
            classReader.accept(onlyReader, ClassReader.EXPAND_FRAMES);

            methodDescriptorMap.putAll(onlyReader.methodDescriptorMap); // 子类会覆盖父类同名方法
        }

        final int interfaceVersion = onlyReader.version;
        final AsmClassMethodDescriptor classDescriptor = onlyReader.classDescriptor;

        // 增强后的interface名称
        String enhancerInterfaceName = enhancerInterfaceName(interfaceClass);

        ClassWriter enhancerWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        EnhancerInterfaceClassVisitor enhancerInterfaceVisitor = new EnhancerInterfaceClassVisitor(enhancerWriter);
        enhancerInterfaceVisitor.visit(interfaceVersion, classDescriptor.access, enhancerInterfaceName.replace(".", "/"), null, classDescriptor.descriptor, new String[0]);

        // 将原interface类上的注解转移到增强类上
        Annotation[] annotations = interfaceClass.getAnnotations();
        CatServerUtil.visitAnnotation(annotations, desc -> enhancerInterfaceVisitor.visitAnnotation(desc, true));

        //如果interface上没有@ResponseBody，在增强类上自动添加一个
        if ( AnnotationUtils.findAnnotation(interfaceClass, ResponseBody.class) == null ) {
            enhancerInterfaceVisitor.visitAnnotation(RESPONSE_BODY, true);
        }

        // 对象包装器类描述
        AbstractResponesWrapper wrapperHandler = serverInfo.getWrapperHandler();
        Class wrapperClass = wrapperHandler != null ? wrapperHandler.getWrapperClass() : null;
        String wrapDesc = wrapperClass != null ? Type.getDescriptor(wrapperClass) : null;

        for ( Method method : interfaceClass.getMethods() ) {

            String methodName = method.getName();
            String signatureId = CatServerUtil.signatureId(method);

            // 得到原interface的方法描述信息
            AsmClassMethodDescriptor methodDescriptor = methodDescriptorMap.get(signatureId);

            /**
             * 增强方法入参处理器
             * 如果是精简模式，处理器会把方法上的入参，处理成一个虚拟对象。入参为虚拟对象的属性；
             * 如果是普通模式，则忽略
             * */
            CatArgumentResolverStrategy resolverStrategy = CatArgumentResolverStrategy.createStrategy(serverInfo.isCatface()).method(method);

            // 方法签名处理器
            SignatureHandler signHandler = new SignatureHandler(methodName, method.getReturnType(), wrapDesc);

            //处理响应结果包装器类
            signHandler.transform(wrapperClass, methodDescriptor.descriptor, methodDescriptor.signature);

            //处理方法入参的虚拟入参class
            signHandler.resolverByStrategy(resolverStrategy);

            /**
             * 为增强后interface，创建方法
             * 方法名、抛出异常不变，修改了方法的响应对象、入参类型
             * */
            MethodVisitor methodVisitor = enhancerInterfaceVisitor.visitMethod(methodDescriptor.access, signHandler.getName(), signHandler.getDesc(), signHandler.getSign(), methodDescriptor.exceptions);

            // 将原方法上注解，转移到增强方法上
            Annotation[] anns = method.getAnnotations();
            CatServerUtil.visitAnnotation(anns, desc -> methodVisitor.visitAnnotation(desc, true));

            // 如果是精简模式
            if( serverInfo.isCatface() ){
                if ( resolverStrategy.hasParameter() ) { // 原方法上存在有效入参，添加 REQUEST_BODY、VALID 注解
                    methodVisitor.visitParameterAnnotation(0, REQUEST_BODY, true);
                    if( HAS_VAILD ){
                        methodVisitor.visitParameterAnnotation(0, VALID, true);
                    }
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

            //创建虚拟入参
            resolverStrategy.createVirtualParameterClass();

            CatMethodInfo methodInfo = new CatMethodInfo();
            methodInfo.setResolverStrategy(resolverStrategy);
            methodInfo.setInterfaceSignatureId(signatureId);
            methodInfo.setEnhancerSignatureId(CatServerUtil.signatureId(methodName, signHandler.getDesc()));

            result.putCatMethodInfo(methodInfo);
        }

        enhancerInterfaceVisitor.visitEnd();

        byte[] newbs = enhancerWriter.toByteArray();
        Class gen = ReflectUtils.defineClass(enhancerInterfaceName, newbs, classLoader);
        printClass(gen, newbs);

        result.setEnhancerClass(gen);
        return result;
    }


    /**
     * 收集方法签名信息
     * */
    private static class OnlyReaderClassVisitor extends ClassVisitor implements Opcodes {

        private int version;
        private AsmClassMethodDescriptor classDescriptor;
        private Map<String, AsmClassMethodDescriptor> methodDescriptorMap = new HashMap<>();

        public OnlyReaderClassVisitor(ClassVisitor classVisitor) {
            super(ASM6, classVisitor);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.version = version;
            this.classDescriptor = new AsmClassMethodDescriptor(access, superName, signature, interfaces);
        }

        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            String desc = transformReturn(descriptor);
            String sign = transformReturn(signature);
            AsmClassMethodDescriptor methodDesc = new AsmClassMethodDescriptor(access, desc, sign, exceptions);
            this.methodDescriptorMap.put(CatServerUtil.signatureId(name, desc), methodDesc);
            return null;
        }

    }


    /**
     * 类、方法签名信息
     * */
    private static class AsmClassMethodDescriptor {

        private final int access;
        private final String descriptor;
        private final String signature;
        private final String[] exceptions;

        public AsmClassMethodDescriptor(int access, String descriptor, String signature, String[] exceptions) {
            this.access = access;
            this.descriptor = descriptor;
            this.signature = signature;
            this.exceptions = exceptions;
        }
    }


    /**
     * 增强interface ClassVisitor
     * */
    private static class EnhancerInterfaceClassVisitor extends ClassVisitor implements Opcodes{
        public EnhancerInterfaceClassVisitor(ClassVisitor cv) {
            super(ASM6, cv);
        }
    }


    /**
     * 方法签名增强
     * */
    private static class SignatureHandler {

        private String name;        // 方法名
        private Class returnType;   // 方法返回对象Type
        private String wrapDesc;    // 包装器描述
        private String desc;    // 方法Type描述
        private String sign;    // 方法签名，包含返回对象、入参泛型信息

        public SignatureHandler(String name, Class returnType, String wrapDesc) {
            this.name = name;
            this.returnType = returnType;
            this.wrapDesc = wrapDesc;
        }

        /**
         * @param descriptor 描述，没有泛型信息
         * @param signature 详细描述，包含泛型信息
         * */
        public void transform(Class wrap, String descriptor, String signature){

            /**
             * 包装器类存在，并且方法返回类型，不是包装器类
             * */
            if( wrapDesc != null && !wrap.isAssignableFrom(returnType) ) {

                String[] desc = descriptor.split("\\)");
                String[] sign = (signature == null ? descriptor : signature).split("\\)");
                String returnSign = wrapDesc.replace(";", "<" + sign[1] + ">;");

                this.desc = desc[0] + ")" + wrapDesc;
                this.sign = sign[0] + ")" + returnSign;

            } else {

                this.desc = descriptor;
                this.sign = signature;

            }
        }

        /**
         * 如果是精简模式，收集原始参数的签名信息
         * resolverByStrategy 处理策略
         * */
        public void resolverByStrategy(CatArgumentResolverStrategy strategy) {
            this.desc = strategy.transformDescriptor(desc);
            this.sign = strategy.transformSignature(sign);
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



    /**
     * 如果方法返回参数为void，则修改成Void
     * 否则不变
     * */
    public static String transformReturn(String desc){
        if( desc != null && desc.endsWith(")V") ){
            return desc.replace(")V", ")Ljava/lang/Void;");
        } else {
            return desc;
        }
    }

    /**
     * 判断是否为增强后对象
     * */
    public static boolean isBridgeClass(Class clazz){
        return clazz != null && clazz.getSimpleName().contains(CatServerUtil.BRIDGE_NAME);
    }

    /**
     * 生成增强后名称
     * */
    private static String enhancerInterfaceName(Class inter){
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
    public static void printClass(Class enhancerClass, byte[] newbs){
        if( debugDir != null ){
            File dir = new File(debugDir + "/" + enhancerClass.getName().replace(".", "/") + ".class");
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
