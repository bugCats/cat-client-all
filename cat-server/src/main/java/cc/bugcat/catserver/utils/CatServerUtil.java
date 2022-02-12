package cc.bugcat.catserver.utils;

import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.Type;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.function.Function;


/**
 *
 * @author bugcat
 * */
@ComponentScan("cc.bugcat.catserver")
public class CatServerUtil implements ApplicationContextAware{


    private static ApplicationContext context;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }


    public static <T> T getBean (Class<T> clazz){
        try {
            return context.getBean(clazz);
        } catch ( Exception e ) {
            Map<String, T> beans = context.getBeansOfType(clazz);
            if( beans.size() == 1 ){
                return beans.values().iterator().next();
            } else {
                for(T value : beans.values()){
                    if( clazz == value.getClass()){
                        return value;
                    }
                    String clazzName = value.getClass().getSimpleName();
                    int start = clazzName.indexOf("$$");
                    if( start > -1 ) {
                        clazzName = clazzName.substring(0, start);
                    }
                    if( clazz.getSimpleName().equals(clazzName) ){
                        return value;
                    }
                }
            }
            throw new NoSuchBeanDefinitionException(clazz);
        }
    }

    public static ClassLoader getClassLoader(){
        return context.getClassLoader();
    }




    /**
     *
     * */
    public static void visitAnnotation(Annotation[] anns, Function<String, AnnotationVisitor> function){
        if( anns.length > 0 ){
            for(Annotation ann : anns){
                Retention retention = AnnotationUtils.getAnnotation(ann, Retention.class);
                if( retention != null && RetentionPolicy.RUNTIME.equals(retention.value()) ){
                    String desc = Type.getDescriptor(ann.annotationType());
                    AnnotationVisitor anv = function.apply(desc);
                    CatServerUtil.visitAnnotation(anv, AnnotationUtils.getAnnotationAttributes(ann));
                    anv.visitEnd();
                }
            }
        }
    }

    public static void visitAnnotation(AnnotationVisitor anv, Map<String, Object> attrMap){
        attrMap.forEach((key, value) -> {
            if( value == null ){
                return;
            }
            annotationValue(anv, key, value);
        });
    }

    private static void annotationValue(AnnotationVisitor anv, String key, Object value){
        if( value == null ){
            return;
        }
        Class clazz = value.getClass();
        if( Proxy.isProxyClass(clazz)){// 如果是jdk动态代理
            return;
        }
        String descriptor = Type.getDescriptor(clazz);
        if( clazz.isEnum() ){
            anv.visitEnum(key, descriptor, ((Enum)value).toString() );
        } else if( clazz.isArray() ){
            AnnotationVisitor array = anv.visitArray(key);
            for(Object arrayValue : (Object[]) value){
                annotationValue(array, key, arrayValue);
            }
            array.visitEnd();
        } else if( value instanceof Class ){
            anv.visit(key, Type.getType((Class) value));
        } else {
            anv.visit(key, value);
        }
    }

}
