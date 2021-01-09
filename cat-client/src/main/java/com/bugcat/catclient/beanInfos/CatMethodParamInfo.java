package com.bugcat.catclient.beanInfos;


import com.bugcat.catface.utils.CatToosUtil;

/**
 * 解析方法上的入参信息
 * @author bugcat
 * */
public class CatMethodParamInfo {

    
    private final String name;    //参数名
    private final int index;      //参数索引值
    private final boolean simple; //是否为String、基本数据类型、包装类
    
    /**
     * 入参参数
     * @param name 参数名
     * @param index 参数索引位置
     * @param pclazz 参数数据类型
     * */
    public CatMethodParamInfo(String name, int index, Class pclazz) {
        this.name = name;
        this.index = index;
        this.simple = CatToosUtil.isSimpleClass(pclazz);
    }
    
    public String getName () {
        return name;
    }
    public int getIndex () {
        return index;
    }
    public boolean isSimple () {
        return simple;
    }
    
}
