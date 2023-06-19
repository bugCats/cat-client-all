package cc.bugcat.catserver.spi;

import java.util.Iterator;
import java.util.ServiceLoader;


/**
 * catface模式虚拟入参getter、setter生成策略
 * */
public interface CatVirtualProprietyPolicy {

    static CatVirtualProprietyPolicy loadService() {
        final ServiceLoader<CatVirtualProprietyPolicy> loaders = ServiceLoader.load(CatVirtualProprietyPolicy.class);
        final Iterator<CatVirtualProprietyPolicy> iterators = loaders.iterator();
        if (iterators.hasNext()) {
            return iterators.next();
        }
        return new SimpleVirtualProprietyPolicy();
    }


    
    /**
     * boolean、Boolean会去掉is前缀。boolean字段方法前缀为is，其他为get；
     * 如果字段首字母小写，第二个字母大写，字段名不变；其他情况需要首字母大写；
     * 其他奇葩字段，自行处理。
     * */
    String getterName(String fieldName, Class type);


    /**
     * boolean、Boolean会去掉is前缀。方法前缀加set；
     * 如果字段首字母小写，第二个字母大写，字段名不变；其他情况需要首字母大写；
     * 其他奇葩字段，自行处理。
     * */
    String setterName(String fieldName, Class type);
    
    
    
    
}
