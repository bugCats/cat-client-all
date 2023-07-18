package cc.bugcat.catserver.handler;

import cc.bugcat.catface.annotation.CatNote;
import cc.bugcat.catface.annotation.CatNotes;
import cc.bugcat.catface.spi.CatClientBridge;
import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.asm.CatServerProperty;
import org.springframework.asm.Type;
import org.springframework.cglib.core.Signature;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author bugcat
 * */
public final class CatMethodInfoBuilder {
    
    
    public static BuilderFactory factory(CatServerProperty serverProperty){
        return new BuilderFactory(serverProperty);
    }
    
    public static class BuilderFactory {


        private final CatServerInfo serverInfo;

        /**
         * serverBean的class。
         * 如果server对象被其他组件动态代理，则为代理后的class！
         * serverBeanClass 不一定等于 serverClass
         * */
        private final Class serverBeanClass;

        /**
         * cglib 的动态调用类
         * */
        private final FastClass serverFastClass;

        
        public BuilderFactory(CatServerProperty serverProperty){
            this.serverInfo = serverProperty.getServerInfo();
            this.serverBeanClass = serverProperty.getServerBean().getClass();
            if( ClassUtils.isCglibProxy(serverBeanClass) == false ){// server对象，没有、或者不是cglib代理，使用快速处理类
                this.serverFastClass = FastClass.create(serverBeanClass);
            } else {
                this.serverFastClass = null;
            }
        }
        
        public CatMethodInfoBuilder builder(){
            return new CatMethodInfoBuilder(serverInfo, serverBeanClass, serverFastClass);
        }
    }



    
    private final CatServerInfo serverInfo;
    private final Class serverBeanClass;
    private final FastClass serverFastClass;

    private CatMethodInfoBuilder(CatServerInfo serverInfo, Class serverBeanClass, FastClass serverFastClass) {
        this.serverInfo = serverInfo;
        this.serverBeanClass = serverBeanClass;
        this.serverFastClass = serverFastClass;
    }

    /**
     * 原interface的方法
     * */
    protected StandardMethodMetadata interfaceMethod;

    /**
     * cglib生成的ctrl类方法
     * */
    protected Method controllerMethod;
    
    /**
     * 原server类的方法
     * */
    protected Method serverMethod;

    /**
     * controller快速调用server对象方法的
     * */
    protected CatServiceMethodProxy serviceMethodProxy;

    /**
     * {@code @CatNote}注解信息
     * */
    protected Map<String, String> noteMap;
    
    /**
     * 方法上参数列表
     * */
    protected Map<String, Integer> paramIndex;

    /**
     * 原interface的方法
     * */
    public CatMethodInfoBuilder interfaceMethod(StandardMethodMetadata interfaceMethod) {
        this.interfaceMethod = interfaceMethod;
        return this;
    }
    
    /**
     * cglib生成的ctrl类方法
     * */
    public CatMethodInfoBuilder controllerMethod(Method controllerMethod) {
        this.controllerMethod = controllerMethod;
        return this;
    }
    
    /**
     * server类的方法
     * */
    public CatMethodInfoBuilder serverMethod(Method serverMethod) {
        this.serverMethod = serverMethod;
        return this;
    }
    
    /**
     * 生成方法描述信息对象，之后把临时缓存清空
     * */
    public CatMethodInfo build(){

        Method method = interfaceMethod.getIntrospectedMethod();
        if( serverFastClass != null ){
            // server对象，没有、或者不是cglib代理，使用快速处理类
            FastMethod fastMethod = serverFastClass.getMethod(method);
            this.serviceMethodProxy = CatServiceMethodProxy.getFastProxy(fastMethod);
        } else {
            // server对象，被cglib代理
            MethodProxy proxy = MethodProxy.find(serverBeanClass, new Signature(method.getName(), Type.getMethodDescriptor(method)));
            this.serviceMethodProxy = CatServiceMethodProxy.getCglibProxy(proxy);
        }

        this.paramIndex = new LinkedHashMap<>();
        Parameter[] parameters = method.getParameters();
        for ( int idx = 0; idx < parameters.length; idx++ ) {
            Parameter parameter = parameters[idx];
            //获取参数名称 interface被编译之后，方法上的参数名会被擦除，只能使用注解标记别名
            String pname = CatToosUtil.getAnnotationValue(parameter, RequestParam.class, ModelAttribute.class, CatNote.class);
            if ( CatToosUtil.isBlank(pname) ) {
                if ( serverInfo.isCatface() ) { // 如果是精简模式，所有的入参统一使用arg0、arg1、arg2、argX...命名
                    pname = "arg" + idx;
                } else {
                    pname = parameter.getName();
                }
            }
            paramIndex.put(pname,Integer.valueOf(idx));
        }

        
        CatNote[] notes = null;
        CatNotes.Group noteGroup = AnnotationUtils.findAnnotation(method, CatNotes.Group.class);
        if( noteGroup != null ){
            notes = CatToosUtil.getCatNotes(noteGroup, CatNotes.Scope.Cilent);
        } else {
            CatClientBridge clientBridge = CatToosUtil.getClientBridge();
            notes = clientBridge.findCatNotes(method);
        }
        // 其他自定义参数、标记
        Map<String, String> noteMap = new LinkedHashMap<>();
        if( notes != null ){
            for ( CatNote note : notes ) {
                String value = CatToosUtil.defaultIfBlank(note.value(), "");

                //如果 key属性为空，默认赋值value
                String key = CatToosUtil.isBlank(note.key()) ? value : note.key();
                noteMap.put(key, value);
            }
        }
        this.noteMap = noteMap;

        return new CatMethodInfo(this);
    }


}
