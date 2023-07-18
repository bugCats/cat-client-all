package cc.bugcat.catserver.utils;

import cc.bugcat.catface.spi.CatClientBridge;
import cc.bugcat.catserver.asm.CatInterfaceEnhancer;
import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.Type;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;


/**
 * cat-server 工具类
 *
 *
 * @author bugcat
 * */
public class CatServerUtil implements ApplicationContextAware{

    /**
     * 通过cglib动态生成的类名、方法名后缀
     * */
    public static final String BRIDGE_NAME = "_byBugcat";
    
    /**
     * interface方法上包含的注解
     * */
    public static final String REQUEST_MAPPING = RequestMapping.class.getName();

    
    /**
     * 自定义组件容器
     * 非spring容器时使用
     * */
    private static Map<Class, Object> catServerMap = new ConcurrentHashMap<>();

    /**
     * spring容器
     * */
    private static ApplicationContext context;

    @Override
    public synchronized void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if ( context == null ) {
            context = applicationContext;
        }
    }



    /**
     * 优先从Spring容器中获取；
     * 其次catServerMap；
     * 都没有则返回null。
     * */
    public static <T> T getBean(Class<T> clazz){
        try {
            return context.getBean(clazz);
        } catch ( Exception ex ) {
            try {
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
            } catch ( Exception e ) {

            }
            return (T) catServerMap.get(clazz);
        }
    }

    /**
     * catServerMap注册bean
     * */
    public static void registerBean(Class type, Object bean){
        catServerMap.putIfAbsent(type, bean);
    }

    /**
     * catServerMap是否包含
     * */
    public static boolean contains(Class key) {
        return catServerMap.containsKey(key);
    }




    public static ClassLoader getClassLoader(){
        return CatServerUtil.class.getClassLoader();
    }

    
    /**
     * 检测 className 是否存在，存在则执行consumer
     * */
    public static <R> R existClassAndExecute(String className, Function<Class, R> function){
        try {
            Class clazz = getClassLoader().loadClass(className);
            return function.apply(clazz);
        } catch ( Exception ex ) {
            return function.apply(null);
        }
    }


    /**
     * 获取方法签名id
     * */
    public static String methodSignature(Method method){
        return method.getName() + "@" + CatInterfaceEnhancer.transformReturn(Type.getMethodDescriptor(method));
    }
    public static String methodSignature(String methodName, String descriptor){
        return methodName + "@" + CatInterfaceEnhancer.transformReturn(descriptor);
    }

    
    /**
     * 给AnnotationVisitor添加注解
     * */
    public static void visitAnnotation(Annotation[] anns, Function<String, AnnotationVisitor> function){
        if( anns.length > 0 ){
            for(Annotation ann : anns){
                Retention retention = AnnotationUtils.getAnnotation(ann, Retention.class);
                if( retention != null && RetentionPolicy.RUNTIME.equals(retention.value()) ){
                    String desc = Type.getDescriptor(ann.annotationType());
                    AnnotationVisitor anv = function.apply(desc);
                    visitAnnotation(anv, AnnotationUtils.getAnnotationAttributes(ann));
                    anv.visitEnd();
                }
            }
        }
    }

    /**
     * 通过AnnotationAttributes，添加注解
     * */
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
        if( value instanceof Annotation ){// 如果是jdk动态代理
            Annotation ann = (Annotation) value;
            AnnotationVisitor van = anv.visitAnnotation(key, Type.getDescriptor(ann.annotationType()));
            visitAnnotation(van, AnnotationUtils.getAnnotationAttributes(ann));
            van.visitEnd();
        } else if( clazz.isEnum() ){
            String descriptor = Type.getDescriptor(clazz);
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
