package cc.bugcat.catserver.handler;

import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.asm.CatVirtualParameterEnhancer;
import cc.bugcat.catserver.spi.CatParameterResolver;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 精简模式下，把方法上所有的入参，处理成一个虚拟对象的属性
 *
 * CatParameterResolverStrategy 默认非精简模式
 *
 * @author bugcat
 * */
public class CatParameterResolverStrategy {

    private final static Pattern trimParameter = Pattern.compile("\\(([^\\)]*)\\)");


    /**
     * 入参处理器策略
     * */
    public static CatParameterResolverStrategy createStrategy(boolean isCatface){
        CatParameterResolverStrategy strategy = null;
        if( isCatface ){
            strategy = new ResolverStrategy();
        } else {
            strategy = new CatParameterResolverStrategy();
        }
        return strategy;
    }




    public CatParameterResolver createParameterResolver(){
        return CatServerDefaults.DEFAULT_RESOLVER;
    }
    public CatParameterResolverStrategy method(Method method){
        return this;
    }
    public String transformDescriptor(String desc) {
        return desc;
    }
    public String transformSignature(String sign){
        return sign;
    }
    public boolean hasParameter(){
        return false;
    }
    public CatParameterResolverStrategy createVirtualParameterClass() {
        return this;
    }
    public String[] getAndResolverDescriptor(){
        return new String[0];
    }
    public String[] getAndResolverSignature() {
        return new String[0];
    }
    public String getClassName() {
        return null;
    }
    public Method getMethod() {
        return null;
    }



    /**
     * 精简模式
     * */
    private static final class ResolverStrategy extends CatParameterResolverStrategy {

        /**
         * 原interface方法信息
         * */
        private Method method;

        /**
         * 虚拟参数对象类名
         * cc.bugcat.example.api.FaceDemoService_Virtual_param0
         * */
        private String className;

        /**
         * 虚拟参数对象类描述
         * Lcc/bugcat/example/api/FaceDemoService_Virtual_param0;
         * */
        private String classDesc;

        /**
         * 原方法入参对象Type描述。通过Type.getType()方法，只能获取描述，泛型会丢失
         * 入参1描述;入参2描述;入参3描述;
         * */
        private String descriptor;

        /**
         * 原方法入参对象签名信息
         * 入参1签名<泛型;>;入参2签名<泛型;>;入参3签名<泛型;>;
         * */
        private String signature;


        /**
         * 创建虚拟参数处理策略对象
         * @param method  原interface的方法
         * */
        @Override
        public CatParameterResolverStrategy method(Method method) {
            this.method = method;
            this.className = method.getDeclaringClass().getName() + "_Virtual_" + method.getName();
            this.classDesc = "L" + className.replace(".", "/") + ";";
            return this;
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
            return matcherAndReplace(desc, matched -> {
                this.descriptor = matched;
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
         * 此处是通过ClassVisitor展开原interface获取到的
         * @see cc.bugcat.catserver.asm.CatInterfaceEnhancer.OnlyReaderClassVisitor
         *
         *
         *  @param sign (入参1签名<泛型;>;入参2签名<泛型;>;入参3签名<泛型;>;)响应签名<泛型;>;
         *  @return (虚拟入参签名;)响应签名<泛型;>;
         * */
        @Override
        public String transformSignature(String sign) {
            return matcherAndReplace(sign, matched -> {
                this.signature = matched;
                return this.classDesc;
            });
        }


        /**
         * 如果原interface方法上，存在参数，才创建
         * */
        @Override
        public CatParameterResolverStrategy createVirtualParameterClass() {
            if( hasParameter() ){
                try { CatVirtualParameterEnhancer.generator(this); } catch ( Exception e ) {}
            }
            return this;
        }


        /**
         * controller上入参预处理类。
         * 实际上生成的controller接收一个虚拟入参对象，
         * 实际server方法，为参数0~n列表，需要把虚拟入参对象的属性，转换成 Object[]。
         * */
        @Override
        public CatParameterResolver createParameterResolver() {
            return CatServerDefaults.FACE_RESOLVER;
        }


        /**
         * 原方法上是否有入参
         * */
        @Override
        public boolean hasParameter() {
            return method.getParameterCount() > 0;
        }


        /**
         * 获取虚拟入参属性。{@link ResolverStrategy#transformDescriptor(java.lang.String)}
         *
         * @return [入参1描述, 入参2描述, 入参3描述]
         * */
        @Override
        public String[] getAndResolverDescriptor() {
            return descriptor.split(";");
        }


        /**
         * 获取虚拟入参属性。{@link ResolverStrategy#transformSignature(java.lang.String)}
         *
         * @return [入参1签名<泛型;>, 入参2签名<泛型;>, 入参3签名<泛型;>;]
         * */
        @Override
        public String[] getAndResolverSignature() {
            if( signature == null ){
                return new String[0];
            }
            List<String> list = new ArrayList<>();
            int lastIndex = 0;
            Stack<Boolean> stack = new Stack();
            for(int i = 0; i < signature.length(); i ++ ){
                char cr = signature.charAt(i);
                if( cr == ';' ){
                    if( stack.empty() ){
                        int end = i + 1;
                        list.add(signature.substring(lastIndex, end));
                        lastIndex = end;
                    }
                } else if( cr == '<' ){
                    stack.push(Boolean.TRUE);
                } else if( cr == '>' ){
                    stack.pop();
                }
            }
            return list.toArray(new String[list.size()]);
        }


        @Override
        public String getClassName() {
            return className;
        }


        @Override
        public Method getMethod() {
            return method;
        }

    }


    private static String matcherAndReplace(String text, Function< String, String> function){
        if( text != null ) {
            Matcher matcher = trimParameter.matcher(text);
            matcher.find();
            String matched = matcher.group(1); // 匹配括号内 (i1;i2;i3)o => i1;i2;i3
            String replace = function.apply(matched);
            if( CatToosUtil.isBlank(matched) ){
                return matcher.replaceAll("()");
            } else {
                return matcher.replaceAll("(" + replace + ")");
            }
        } else {
            return text;
        }
    }

}
