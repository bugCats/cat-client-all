package com.bugcat.catclient.beanInfos;


import com.bugcat.catclient.utils.CatToosUtil;

/**
 * 解析方法上的入参信息
 * @author bugcat
 * */
public class CatMethodsParamInfo {

    
    private String name;    //参数名
    private int index;      //参数索引值
    private boolean simple; //是否为String、基本数据类型、包装类
    
    /**
     * 入参参数
     * @param name 参数名
     * @param index 参数索引位置
     * @param pclazz 参数数据类型
     * */
    public CatMethodsParamInfo(String name, int index, Class pclazz) {
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
