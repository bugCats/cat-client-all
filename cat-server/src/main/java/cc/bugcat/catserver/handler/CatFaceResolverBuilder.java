package cc.bugcat.catserver.handler;

import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.asm.CatVirtualParameterEnhancer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 精简模式下，把方法上所有的入参，处理成一个虚拟对象的属性
 * */
public class CatFaceResolverBuilder{

    private final static Pattern trimClassName = Pattern.compile("\\.class$");
    private final static Pattern trimParameter = Pattern.compile("\\(([^\\)]*)\\)");
    
    

    public static CatFaceResolverBuilder builder(boolean isCatface){
        CatFaceResolverBuilder builder = null;
        if( isCatface ){
            builder = new ResolverBuilder();
        } else {
            builder = new CatFaceResolverBuilder();
        }
        return builder;
    }

    
    public CatFaceResolverBuilder method(Method method){
        return this;
    }
    public String descriptor(String desc) {
        return desc;
    }
    public String signature(String sign){
        return sign;
    }
    public CatFaceResolver build(){
        return null;
    }
    public boolean hasParameter(){
        return false;
    }
    public CatFaceResolverBuilder createClass() {
        return this;
    }
    public String[] getDescriptor(){
        return new String[0];
    }
    public String[] getSignature(String[] descs) {
        return new String[0];
    }
    public String getClassName() {
        return null;
    }
    public Method getMethod() {
        return null;
    }


    

    private static class ResolverBuilder extends CatFaceResolverBuilder{

        private Method method;

        private String className;
        private String classDesc;

        private String descriptor;
        private String signature;

        @Override
        public CatFaceResolverBuilder method(Method method) {
            this.method = method;
            Class[] parameterTypes = method.getParameterTypes();
            Class[] params = new Class[parameterTypes.length];
            for(int idx = 0; idx < parameterTypes.length; idx ++ ){
                params[idx] = parameterTypes[idx];
            }
            Class srcClass = method.getDeclaringClass();
            this.className = trimClassName.matcher(srcClass.getName()).replaceAll("") + "Virtual$" + CatToosUtil.capitalize(method.getName());
            this.classDesc = ("L" + className.replace(".", "/") + ";").replace("$", "\\$");
            return this;
        }


        @Override
        public String descriptor(String desc) {
            if( desc != null ) {
                Matcher matcher = trimParameter.matcher(desc);
                matcher.find();
                this.descriptor = matcher.group(1);
                if( CatToosUtil.isBlank(this.descriptor) ){
                    return matcher.replaceAll("()");
                } else {
                    return matcher.replaceAll("(" + this.classDesc + ")");
                }
            } else {
                return desc;
            }
        }

        @Override
        public String signature(String sign) {
            if( sign != null ) {
                Matcher matcher = trimParameter.matcher(sign);
                matcher.find();
                this.signature = matcher.group(1);
                if( CatToosUtil.isBlank(this.signature) ){
                    return matcher.replaceAll("()");
                } else {
                    return matcher.replaceAll("(" + this.classDesc + ")");
                }
            } else {
                return sign;
            }
        }


        @Override
        public CatFaceResolverBuilder createClass() {
            if( method.getParameterCount() > 0 ){
                try { CatVirtualParameterEnhancer.generator(this); } catch ( Exception e ) {}
            }
            return this;
        }


        @Override
        public CatFaceResolver build() {
            return new CatFaceResolver(this);
        }

        @Override
        public boolean hasParameter() {
            return method.getParameterCount() > 0;
        }

        @Override
        public String[] getDescriptor() {
            return descriptor.split(";");
        }

        @Override
        public String[] getSignature(String[] descs) {
            if( signature == null ){
                return new String[descs.length];
            }
            List<String> list = new ArrayList<>(descs.length);
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
    

}
