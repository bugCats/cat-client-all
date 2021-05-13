package com.bugcat.catclient.beanInfos;


import com.bugcat.catface.utils.CatToosUtil;

/**
 * 解析方法上的入参信息
 * @author bugcat
 * */
public class CatMethodParamInfo {

    
    private final int index;      //参数索引值
    private final boolean simple; //是否为String、基本数据类型、包装类
    private boolean primary;    //是否为主要参数？只能容许有一个被@RequestBody、@ModelAttribute 标记的入参
    
    /**
     * 入参参数
     * @param index 参数索引位置
     * @param pclazz 参数数据类型
     * */
    public CatMethodParamInfo(int index, Class pclazz) {
        this.index = index;
        this.simple = CatToosUtil.isSimpleClass(pclazz);
    }
    
    public int getIndex () {
        return index;
    }
    public boolean isSimple () {
        return simple;
    }

    public boolean isPrimary() {
        return primary;
    }
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}
