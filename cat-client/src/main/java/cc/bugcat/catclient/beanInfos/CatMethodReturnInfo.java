package cc.bugcat.catclient.beanInfos;

import cc.bugcat.catface.utils.CatToosUtil;

import java.lang.reflect.Type;

/**
 * 方法返回参数类型
 *
 * @author bugcat
 * */
public class CatMethodReturnInfo {

    /**
     * 参数的类名称
     * */
    private final String name;

    /**
     * 是否为简单对象：String、基本数据类型+包装类
     * */
    private final boolean simple;

    /**
     * 参数class
     * */
    private final Class clazz;

    /**
     * 参数的Type
     * */
    private final Type type;


    public CatMethodReturnInfo(Class clazz, Type type) {
        this.name = clazz.getSimpleName().toUpperCase();
        this.simple = CatToosUtil.isSimpleClass(clazz);
        this.clazz = clazz;
        this.type = type;
    }


    public String getName () {
        return name;
    }
    public boolean isSimple () {
        return simple;
    }
    public Class getClazz () {
        return clazz;
    }
    public Type getType () {
        return type;
    }

}
