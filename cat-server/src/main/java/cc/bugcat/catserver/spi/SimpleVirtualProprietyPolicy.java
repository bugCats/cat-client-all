package cc.bugcat.catserver.spi;

import cc.bugcat.catface.utils.CatToosUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SimpleVirtualProprietyPolicy implements CatVirtualProprietyPolicy {

    // 首字母小写，第二字母大写
    private final static Pattern pattern = Pattern.compile("^[a-z][A-Z]]");
    
    
    private final NomalPolicy nomal = new NomalPolicy();
    private final BooleanPolicy1 bool = new BooleanPolicy1();
    private final BooleanPolicy2 BOOL = new BooleanPolicy2();
    
    
    /**
     * boolean、Boolean会去掉is前缀。boolean字段方法前缀为is，其他为get；
     * 如果字段首字母小写，第二个字母大写，字段名不变；其他情况需要首字母大写；
     * 其他奇葩字段，自行处理。
     * */
    @Override
    public String getterName(String fieldName, Class type) {
        NomalPolicy policy = getNomalPolicy(type);
        return policy.getterPrefix() + policy.fieldAliasName(fieldName);
    }


    /**
     * boolean、Boolean会去掉is前缀。方法前缀加set；
     * 如果字段首字母小写，第二个字母大写，字段名不变；其他情况需要首字母大写；
     * 其他奇葩字段，自行处理。
     * */
    @Override
    public String setterName(String fieldName, Class type) {
        NomalPolicy policy = getNomalPolicy(type);
        return policy.setterPrefix() + policy.fieldAliasName(fieldName);
    }

    
    
    
    
    private NomalPolicy getNomalPolicy(Class type){
        NomalPolicy policy = null;
        if( "boolean".equals(type.getSimpleName().toLowerCase()) ){
            if( type.isPrimitive() ){
                policy = bool;
            } else {
                policy = BOOL;
            }
        } else {
            policy = nomal;
        }
        return policy;
    }

    
    private static class NomalPolicy {
        protected String getterPrefix(){
            return "get";
        }
        protected String setterPrefix(){
            return "set";
        }
        protected String fieldAliasName(String fieldName){
            Matcher matcher = pattern.matcher(fieldName);
            if ( matcher.find() ) { //首字母小写，第二个字母大写
                return fieldName;
            } else {
                return CatToosUtil.capitalize(fieldName);
            }
        }
        
    }
    
    /**
     * 小boolean
     * */
    private static class BooleanPolicy1 extends NomalPolicy {
        @Override
        protected String getterPrefix() {
            return "is";
        }

        @Override
        protected String fieldAliasName(String fieldName) {
            if( fieldName.startsWith("is") ){
                return super.fieldAliasName(fieldName.substring(2));
            } else {
                return super.fieldAliasName(fieldName);
            }
        }
    }
    /**
     * 大Boolean
     * */
    private static class BooleanPolicy2 extends NomalPolicy {
        @Override
        protected String fieldAliasName(String fieldName) {
            if( fieldName.startsWith("is") ){
                return super.fieldAliasName(fieldName.substring(2));
            } else {
                return super.fieldAliasName(fieldName);
            }
        }
    }
}
