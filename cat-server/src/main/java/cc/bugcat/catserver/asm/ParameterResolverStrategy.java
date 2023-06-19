package cc.bugcat.catserver.asm;

import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.handler.CatServerDepend;
import cc.bugcat.catserver.spi.CatParameterResolver;
import cc.bugcat.catserver.utils.CatServerUtil;
import org.springframework.asm.Type;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 精简模式下，把方法上所有的入参，处理成一个虚拟对象的属性。
 *
 * ParameterResolverStrategy 默认非精简模式。
 *
 * @author bugcat
 * */
class ParameterResolverStrategy {

    /**
     * 修改方法入参描述
     * */
    private final static Pattern trimParameter = Pattern.compile("\\(([^\\)]*)\\)");


    /**
     * 入参处理器策略
     * @param isCatface 是否为catface模式
     * @param method interface原始方法
     * */
    public static ParameterResolverStrategy createStrategy(boolean isCatface, Method method){
        ParameterResolverStrategy strategy = null;
        if( isCatface ){
            strategy = new VirtualParameterResolverStrategy(method);
        } else {
            strategy = new ParameterResolverStrategy();
        }
        return strategy;
    }

    

    /**
     * 如果是catface，需要修改增强方法上的参数描述
     * */
    public String transformDescriptor(String desc) {
        return desc;
    }
    public String transformSignature(String sign){
        return sign;
    }
    
    public List<String> getAndResolverDescriptor(){
        return new ArrayList<>(0);
    }
    public List<String> getAndResolverSignature() {
        return new ArrayList<>(0);
    }

    /**
     * 如果是catface，返回虚拟参数类名
     * */
    public String getClassName() {
        return null;
    }
    
    /**
     * 默认的方法处理。
     * 如果是catface，会把原始方法上的参数组装成一个虚拟对象，在执行CatServer对应方法之前，需要转换成入参数组。
     * */
    public CatParameterResolver parameterResolver(){
        return CatServerDepend.DEFAULT_RESOLVER;
    }



    /**
     * 精简模式
     * */
    static final class VirtualParameterResolverStrategy extends ParameterResolverStrategy {

        /**
         * 虚拟参数对象类名
         * cc.bugcat.example.api.virtual.FaceDemoService_param0
         * */
        private final String className;

        /**
         * 虚拟参数对象类描述
         * Lcc/bugcat/example/api/FaceDemoService_param0;
         * */
        private final String classDesc;

        /**
         * 原方法入参对象Type描述。通过Type.getType()方法，只能获取描述，泛型会丢失
         * 入参1描述;入参2描述;入参3描述;
         * */
        private List<String> descriptors;

        /**
         * 原方法入参对象签名信息
         * 入参1签名<泛型;>;入参2签名<泛型;>;入参3签名<泛型;>;
         * */
        private List<String> signatures;

        
        /**
         * 创建虚拟参数处理策略对象
         * @param method  原interface的方法
         * */
        private VirtualParameterResolverStrategy(Method method) {
            this.className = method.getDeclaringClass().getName() + "_" + method.getName();
            this.classDesc = "L" + className.replace(".", "/") + ";";
        }
        
        /**
         * 转换描述
         * method方法的Type描述：(入参1描述;入参2描述;入参3描述;)响应描述;
         *
         * 增强后的interface方法，只有一个虚拟入参对象，因此要把增强后interface的方法描述修改成：入参为虚拟对象
         *  => (虚拟入参描述;)响应描述;
         *
         *
         *  @param desc (入参1描述;入参2描述;入参3描述;)响应描述;
         *  @return (虚拟入参描述;)响应描述;
         * */
        @Override
        public String transformDescriptor(String desc) {
            return typeReplace(desc, argTypes -> {
                this.descriptors = argTypes;
                return this.classDesc;
            });
        }

        /**
         * 转换签名，包含泛型
         * method方法的签名，(入参1签名<泛型;>;入参2签名<泛型;>;入参3签名<泛型;>;)响应签名<泛型;>;
         *
         * 增强后的interface方法，只有一个虚拟入参对象，因此要把增强后interface的方法签名修改成：入参为虚拟对象
         *  => (虚拟入参签名;)响应签名<泛型;>;
         *
         * 通过反射、或者Type.getType()、等普通方法是无法获取到签名信息，
         * 此处是通过ClassVisitor展开原interface获取到的 cc.bugcat.catserver.asm.CatInterfaceEnhancer.OnlyReaderClassVisitor
         *
         *
         *  @param sign (入参1签名<泛型;>;入参2签名<泛型;>;入参3签名<泛型;>;)响应签名<泛型;>;
         *  @return (虚拟入参签名;)响应签名<泛型;>;
         * */
        @Override
        public String transformSignature(String sign) {
            return typeReplace(sign, argTypes -> {
                this.signatures = argTypes;
                return this.classDesc;
            });
        }




        /**
         * 获取虚拟入参属性。{@link VirtualParameterResolverStrategy#transformDescriptor(java.lang.String)}
         *
         * @return [入参1描述, 入参2描述, 入参3描述]
         * */
        @Override
        public List<String> getAndResolverDescriptor() {
            return descriptors;
        }


        /**
         * 获取虚拟入参属性。{@link VirtualParameterResolverStrategy#transformSignature(java.lang.String)}
         *
         * @return [入参1签名<泛型;>, 入参2签名<泛型;>, 入参3签名<泛型;>;]
         * */
        @Override
        public List<String> getAndResolverSignature() {
            return signatures;
        }


        @Override
        public String getClassName() {
            return className;
        }

        /**
         * controller上入参预处理类。
         * 实际上生成的controller接收一个虚拟入参对象，
         * 实际server方法，为参数0~n列表，需要把虚拟入参对象的属性，转换成 Object[]。
         * */
        @Override
        public CatParameterResolver parameterResolver() {
            return CatServerDepend.FACE_RESOLVER;
        }

    }


    private static String typeReplace(String text, Function<List<String>, String> function){
        if( text != null ) {
            List<String> argTypes = CatTypeTools.getArgumentTypes(text);
            if( argTypes.size() > 0 ){
                String replace = function.apply(argTypes);
                return trimParameter.matcher(text).replaceFirst("(" + replace + ")");
            }
        }
        return text;
    }

}
