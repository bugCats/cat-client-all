package cc.bugcat.catserver.asm;

import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.handler.CatArgumentResolverStrategy;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * 精简模式下，把原方法上所有的入参，处理成一个虚拟对象的属性。
 * 并且尽可能将入参上的注解，转移到虚拟入参属性上
 *
 *
 * 原方法：
 *  UserInfo queryByUser(UserPageVi user, UserPageVi page, Integer status, Map<String, Object> reqMap);
 *
 *
 *
 * 虚拟入参对象：
 *  class UserInfo_Virtual_queryByUser implements VirtualParameter {
 *
 *      private UserPageVi arg0;
 *      private UserPageVi arg1;
 *      private Integer arg2;
 *      private Map<String, Object> arg3;
 *
 *      public Object[] toArray(){
 *          return new Object[]{arg0, arg1, arg2, arg3};
 *      }
 *
 *      ...getter...setter...
 *  }
 *
 *
 * 增强后方法：
 *  UserInfo queryByUser(@RequestBody UserInfo_Virtual_queryByUser req);
 *
 *
 * */
public class CatVirtualParameterEnhancer {


    public static Class generator(CatArgumentResolverStrategy strategy) throws Exception {

        ClassLoader classLoader = CatServerUtil.getClassLoader();

        InputStream stream = classLoader.getResourceAsStream(NoOp.class.getName().replace(".", "/") + ".class");
        ClassReader cr = new ClassReader(stream);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);

        String className = strategy.getClassName();
        Method method = strategy.getMethod();
        String[] descs = strategy.getAndResolverDescriptor();   // 方法上所有入参的Type描述信息，转换成字段描述
        String[] signs = strategy.getAndResolverSignature();    // 方法上所有入参的签名信息，转换成字段签名

        Class[] parameterType = method.getParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();

        ClassVisitor visitor = new VirtualParameterClassVisitor(cw, className);
        MethodBuilder methodBuilder = new MethodBuilder(className);
        FieldSignature[] fields = new FieldSignature[parameterType.length];

        for(int idx = 0; idx < parameterType.length; idx ++ ){

            String fieldName = "arg" + idx;                   // 虚拟入参对象，属性名
            String alias = CatToosUtil.capitalize(fieldName); // 首字母大写
            String desc = descs[idx] + ";";
            String sign = signs[idx];

            FieldSignature field = new FieldSignature();    // 字段描述信息
            field.fieldName = fieldName;
            field.descriptor = desc;
            field.signature = sign;
            field.resolve(annotations[idx]);
            fields[idx] = field;

            FieldSignature getter = new FieldSignature();   // get方法描述信息
            getter.fieldName = fieldName;
            getter.methodName = "get" + alias;
            getter.descriptor = "()" + desc;
            getter.signature = "()" + sign;
            getter.field = field;

            FieldSignature setter = new FieldSignature();   // set方法描述信息
            setter.fieldName = fieldName;
            setter.methodName = "set" + alias;
            setter.descriptor = "(" + desc + ")V";
            setter.signature = "(" + sign + ")V";
            setter.field = field;

            // 尽可能多的将入参上注解，转换到类的属性上
            FieldVisitor fieldVisitor = visitor.visitField(Opcodes.ACC_PRIVATE, fieldName, field.descriptor, field.signature, null);
            for( AnnotationResolver resolver : field.annotationResolvers ){
                AnnotationVisitor anv = fieldVisitor.visitAnnotation(resolver.typeDesc, true);
                CatServerUtil.visitAnnotation(anv, resolver.attrMap);
                anv.visitEnd();
            }

            fieldVisitor.visitEnd();

            methodBuilder.getMethod(visitor, getter);
            methodBuilder.setMethod(visitor, setter);
        }

        // 将虚拟入参对象属性，转换成入参数组对象
        methodBuilder.toArrayMethod(visitor, fields);
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
        }

    }


    private static class MethodBuilder implements Opcodes{

        private final String classType;   // com/bugcat/example/asm/DemoUser
        private final String classDesc;   // Lcom/bugcat/example/asm/DemoUser;

        public MethodBuilder(String className) {
            this.classType = className.replace(".", "/");
            this.classDesc = "L" + classType + ";";
        }

        /**
         * 创建getter方法
         * */
        public void getMethod(ClassVisitor visitor, FieldSignature getter){
            MethodVisitor mv = visitor.visitMethod(ACC_PUBLIC, getter.methodName, getter.descriptor, getter.signature, null);
            mv.visitCode();
            Label label0 = new Label();
            mv.visitLabel(label0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, classType, getter.fieldName, getter.field.descriptor);
            mv.visitInsn(ARETURN);
            Label lebel1 = new Label();
            mv.visitLabel(lebel1);
            mv.visitLocalVariable("this", classDesc, null, label0, lebel1, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

        /**
         * 创建setter方法
         * */
        public void setMethod(ClassVisitor visitor, FieldSignature setter){
            MethodVisitor mv = visitor.visitMethod(ACC_PUBLIC, setter.methodName, setter.descriptor, setter.signature, null);
            mv.visitCode();
            Label label0 = new Label();
            mv.visitLabel(label0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, classType, setter.fieldName, setter.field.descriptor);
            mv.visitInsn(RETURN);
            Label lebel1 = new Label();
            mv.visitLabel(lebel1);
            mv.visitLocalVariable("this", classDesc, null, label0, lebel1, 0);
            mv.visitLocalVariable(setter.fieldName, setter.field.descriptor, setter.field.signature, label0, lebel1, 1);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }

        /**
         * 创建VirtualParameter#toArray()方法
         * */
        public void toArrayMethod(ClassVisitor visitor, FieldSignature[] fields){
            MethodVisitor mv = visitor.visitMethod(ACC_PUBLIC, "toArray", "()[Ljava/lang/Object;", null, null);
            mv.visitCode();
            Label Label0 = new Label();
            mv.visitLabel(Label0);
            mv.visitIntInsn(BIPUSH, fields.length);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

            for ( int idx = 0; idx < fields.length; idx ++ ) {
                FieldSignature field = fields[idx];
                mv.visitInsn(DUP);
                mv.visitIntInsn(BIPUSH, idx);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, classType, field.fieldName, field.descriptor);
                mv.visitInsn(AASTORE);
            }

            mv.visitInsn(ARETURN);

            Label Label1 = new Label();
            mv.visitLabel(Label1);
            mv.visitLocalVariable("this", classDesc, null, Label0, Label1, 0);
            mv.visitMaxs(4, 1);
            mv.visitEnd();

        }
    }


    /**
     * 字段、方法的描述与签名信息
     * */
    private static class FieldSignature {

        private String fieldName;   // 字段名
        private String descriptor;  // type描述  Ljava/lang/String;
        private String signature;   // 包含泛型
        private List<AnnotationResolver> annotationResolvers;   //字段上的注解

        private String methodName;      //get、set方法名
        private FieldSignature field;   // get、set方法对应的字段

        /**
         * 将方法上入参的注解，转移到属性上
         * */
        public void resolve(Annotation[] annotations){

            this.annotationResolvers = new ArrayList<>(annotations.length);

            for ( Annotation annotation : annotations ) {
                String annName = annotation.annotationType().getSimpleName().toLowerCase();

                if( "validated".equals(annName) && CatInterfaceEnhancer.HAS_VAILD ){ // 验证框架

                    annotationResolvers.add(new AnnotationTransformResolver(annotation,
                            "Ljavax/validation/Valid;",
                            new String[0]));

                } else if("apiparam".equals(annName)){ // swagger

                    annotationResolvers.add(new AnnotationTransformResolver(annotation,
                            "Lio/swagger/annotations/ApiModelProperty;",
                            "value", "name", "allowableValues", "access", "required" , "hidden", "example"));

                } else {

                    //  其他可以添加到field上的注解
                    Target target = AnnotationUtils.getAnnotation(annotation, Target.class);
                    ElementType[] types = target.value();
                    for(ElementType type : types){
                        if( type == ElementType.FIELD ){
                            annotationResolvers.add(new AnnotationResolver(annotation));
                            break;
                        }
                    }
                }
            }
        }
    }


    /**
     * 全部转换
     * */
    private static class AnnotationResolver {

        protected String typeDesc;
        protected Map<String, Object> attrMap;

        public AnnotationResolver(Annotation annotation) {
            this.typeDesc = Type.getDescriptor(annotation.annotationType());
            this.attrMap = AnnotationUtils.getAnnotationAttributes(annotation);
        }
    }


    /**
     * 转换注解的部分属性
     * */
    private static class AnnotationTransformResolver extends AnnotationResolver {

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
     * 虚拟入参对象模板
     * */
    public static class NoOp {}


    /**
     * 虚拟入参对象
     * */
    public static interface VirtualParameter {
        public Object[] toArray();
    }

}
