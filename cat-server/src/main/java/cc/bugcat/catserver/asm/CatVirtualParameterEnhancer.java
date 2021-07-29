package cc.bugcat.catserver.asm;

import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.handler.CatFaceResolverBuilder;
import cc.bugcat.catserver.utils.CatServerUtil;
import org.springframework.asm.*;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 精简模式下，把方法上所有的入参，处理成一个虚拟对象的属性
 * */
public class CatVirtualParameterEnhancer{
    
    
    private final static String baseClass = Noop.class.getName().replace(".", "/");
    
    
    public static Class generator(CatFaceResolverBuilder builder) throws Exception {
        
        ClassLoader classLoader = CatServerUtil.getClassLoader();
        InputStream stream = classLoader.getResourceAsStream(baseClass + ".class");
        
        ClassReader cr = new ClassReader(stream);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        
        String[] descs = builder.getDescriptor();
        String[] signs = builder.getSignature(descs);
        
        String className = builder.getClassName();
        Method method = builder.getMethod();
        Class[] parameterType = method.getParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        
        ClassVisitor visitor = new VirtualParameterClassVisitor(cw, className);
        MethodBuilder methodBuilder = new MethodBuilder(className);
        FieldSignature[] fields = new FieldSignature[parameterType.length];
        
        for(int idx = 0; idx < parameterType.length; idx ++ ){

            String fieldName = "arg" + idx;
            String alias = CatToosUtil.capitalize(fieldName);
            String desc = descs[idx] + ";";
            String sign = signs[idx];

            FieldSignature field = new FieldSignature();
            field.fieldName = fieldName;
            field.descriptor = desc;
            field.signature = sign;
            field.setAnnotations(annotations[idx]);
            fields[idx] = field;

            FieldSignature getter = new FieldSignature();
            getter.fieldName = fieldName;
            getter.methodName = "get" + alias;
            getter.descriptor = "()" + desc;
            getter.signature = "()" + sign;
            getter.field = field;

            FieldSignature setter = new FieldSignature();
            setter.fieldName = fieldName;
            setter.methodName = "set" + alias;
            setter.descriptor = "(" + desc + ")V";
            setter.signature = "(" + sign + ")V";
            setter.field = field;

            FieldVisitor fieldVisitor = visitor.visitField(Opcodes.ACC_PRIVATE, fieldName, field.descriptor, field.signature, null);
            if(field.resolvers != null && field.resolvers.size() > 0){
                for( AnnotationResolver resolver : field.resolvers){
                    AnnotationVisitor anv = fieldVisitor.visitAnnotation(resolver.typeDesc, true);
                    CatServerUtil.visitAnnotation(anv, resolver.attrMap);
                    anv.visitEnd();
                }
            }
            fieldVisitor.visitEnd();

            methodBuilder.getMethod(visitor, getter);
            methodBuilder.setMethod(visitor, setter);
        }
        
        methodBuilder.invokeMethod(visitor, fields);
        visitor.visitEnd();

        cr.accept(visitor, ClassReader.EXPAND_FRAMES);
        byte[] newbs = cw.toByteArray();
        Class gen = ReflectUtils.defineClass(className, newbs, classLoader);
        CatInterfaceEnhancer.printClass(gen, newbs);

        return gen;
    }
    

    private static class VirtualParameterClassVisitor extends ClassVisitor implements Opcodes{

        private String className;

        public VirtualParameterClassVisitor(ClassVisitor cv, String className) {
            super(ASM6, cv);
            this.className = className.replace(".", "/");
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            String interType = VirtualParameter.class.getName().replace(".", "/");
            super.visit(version, ACC_PUBLIC, className, signature, superName, new String[]{interType});
            super.visitInnerClass(interType,
                    CatVirtualParameterEnhancer.class.getName().replace(".", "/"),
                    VirtualParameter.class.getSimpleName(),
                    ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT + ACC_INTERFACE);
        }

    }



    private static class MethodBuilder implements Opcodes{

        private final String classType;   // com/bugcat/example/asm/DemoUser
        private final String classDesc;   // Lcom/bugcat/example/asm/DemoUser;
        private int curLine = 10;

        public MethodBuilder(String className) {
            this.classType = className.replace(".", "/");
            this.classDesc = "L" + classType + ";";
        }

        public void getMethod(ClassVisitor visitor, FieldSignature getter){
            MethodVisitor methodVisitor = visitor.visitMethod(ACC_PUBLIC, getter.methodName, getter.descriptor, getter.signature, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(curLine, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, classType, getter.fieldName, getter.field.descriptor);
            methodVisitor.visitInsn(ARETURN);
            Label lebel1 = new Label();
            methodVisitor.visitLabel(lebel1);
            methodVisitor.visitLocalVariable("this", classDesc, null, label0, lebel1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
            curLine = curLine + 3;
        }

        public void setMethod(ClassVisitor visitor, FieldSignature setter){
            MethodVisitor methodVisitor = visitor.visitMethod(ACC_PUBLIC, setter.methodName, setter.descriptor, setter.signature, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(curLine, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitFieldInsn(PUTFIELD, classType, setter.fieldName, setter.field.descriptor);
            methodVisitor.visitInsn(RETURN);
            Label lebel1 = new Label();
            methodVisitor.visitLabel(lebel1);
            methodVisitor.visitLocalVariable("this", classDesc, null, label0, lebel1, 0);
            methodVisitor.visitLocalVariable(setter.fieldName, setter.field.descriptor, setter.field.signature, label0, lebel1, 1);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();
            curLine = curLine + 3;
        }

        public void invokeMethod(ClassVisitor visitor, FieldSignature[] fields){

            MethodVisitor mv = visitor.visitMethod(Opcodes.ACC_PUBLIC, "invoke", "(I)Ljava/lang/Object;", null, null);

            mv.visitCode();
            Label label0 = new Label();
            mv.visitLabel(label0);
            mv.visitLineNumber(curLine++, label0);
            mv.visitVarInsn(ILOAD, 1);

            Label[] cases = new Label[fields.length];
            for ( int i = 0; i < cases.length; i ++ ) {
                cases[i] = new Label();
            }

            Label defaultLabel = new Label();
            mv.visitTableSwitchInsn(0, cases.length - 1, defaultLabel, cases);

            for( int i = 0; i < cases.length; i ++ ){
                FieldSignature field = fields[i];
                mv.visitLabel(cases[i]);
                mv.visitLineNumber(curLine++ , cases[i]);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, classType, field.fieldName, field.descriptor);
                mv.visitInsn(ARETURN);
            }


            mv.visitLabel(defaultLabel);
            mv.visitLineNumber(curLine++, defaultLabel);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);

            Label label = new Label();
            mv.visitLabel(label);
            mv.visitLocalVariable("this", classDesc, null, label0, label, 0);
            mv.visitLocalVariable("index", "I", null, label0, label, 1);
            mv.visitMaxs(1, 2);

            mv.visitEnd();
        }

    }



    private static class FieldSignature {

        private String fieldName;
        private String methodName;
        private String descriptor;
        private String signature;

        private FieldSignature field;
        private List<AnnotationResolver> resolvers;
        
        /**
         * 将方法上入参的注解，转移到属性上
         * */
        public void setAnnotations(Annotation[] annotations){
            this.resolvers = new ArrayList<>(annotations.length);
            Arrays.stream(annotations).forEach(ann -> {
                String annName = ann.annotationType().getSimpleName().toLowerCase();
                if( "validated".equals(annName) && CatInterfaceEnhancer.hasVaild ){
                    resolvers.add(new AnnotationTransformResolver(ann, "Ljavax/validation/Valid;", new String[0]));
                } else if("apiparam".equals(annName)){
                    resolvers.add(new AnnotationTransformResolver(ann, "Lio/swagger/annotations/ApiModelProperty;",
                            "value", "name", "allowableValues", "access", "required" , "hidden", "example"));
                } else {
                    Target target = AnnotationUtils.getAnnotation(ann, Target.class);
                    ElementType[] types = target.value();
                    for(ElementType type : types){
                        if( type == ElementType.FIELD ){
                            resolvers.add(new AnnotationResolver(ann));
                            break;
                        }
                    }
                }
            });
        }
    }

    
    private static class AnnotationResolver {
        protected String typeDesc;
        protected Map<String, Object> attrMap;
        public AnnotationResolver(Annotation annotation) {
            this.typeDesc = Type.getDescriptor(annotation.annotationType());
            this.attrMap = AnnotationUtils.getAnnotationAttributes(annotation);
        }
    }

    private static class AnnotationTransformResolver extends AnnotationResolver {
        public AnnotationTransformResolver(Annotation annotation, String typeDesc) {
            this(annotation, typeDesc, null);
        }
        public AnnotationTransformResolver(Annotation annotation, String typeDesc, String... props) {
            super(annotation);
            this.typeDesc = typeDesc;
            if( props != null ) {
                Map<String, Object> attrMap = new HashMap<>();
                for(String prop : props){
                    attrMap.put(prop, this.attrMap.get(prop));
                }
                this.attrMap = attrMap;
            }
        }
    }


    /**
     * 模板
     * */
    public static class Noop{}
    
    public static interface VirtualParameter{
        public Object invoke(int index);
    }

}
