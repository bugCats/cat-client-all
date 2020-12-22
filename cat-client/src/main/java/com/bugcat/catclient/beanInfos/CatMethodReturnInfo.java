package com.bugcat.catclient.beanInfos;

import com.bugcat.catclient.utils.CatClientUtil;
import com.bugcat.catface.utils.CatToosUtil;

import java.lang.reflect.Type;

/**
 * 方法返回参数类型
 * @author bugcat
 * */
public class CatMethodReturnInfo {
    
    private String name;    //参数的类名称
    private boolean simple; //是否为简单对象：String、基本数据类型+包装类
    private Class clazz;    //参数class
    private Type type;      //参数的Type

 
    public CatMethodReturnInfo(Class clazz, Type type) {
        this.clazz = clazz;
        this.name = clazz.getSimpleName().toUpperCase();
        this.type = type;
        this.simple = CatToosUtil.isSimpleClass(clazz);
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
