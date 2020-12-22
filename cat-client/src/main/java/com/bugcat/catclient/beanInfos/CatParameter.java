package com.bugcat.catclient.beanInfos;


import com.bugcat.catclient.spi.CatHttp;

import java.util.Map;

/**
 * 多例
 * @author bugcat
 * */
public class CatParameter {
    
    private String path;    //真实url，PathVariable已经处理
    private Object value;   //经过处理之后的有效参数
    private Map<String, Object> argMap; //原始的参数列表
    private CatHttp catHttp;    //http请求类
    
    
    
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }

    public Map<String, Object> getArgMap() {
        return argMap;
    }
    public void setArgMap(Map<String, Object> argMap) {
        this.argMap = argMap;
    }

    public CatHttp getCatHttp() {
        return catHttp;
    }
    public void setCatHttp(CatHttp catHttp) {
        this.catHttp = catHttp;
    }
}
