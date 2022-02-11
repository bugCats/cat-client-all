package cc.bugcat.catclient.beanInfos;


import java.util.Map;


/**
 * 每次http请求解析后的参数对象
 *
 * @author bugcat
 * */
public class CatParameter {

    /**
     * 真实url，PathVariable已经处理
     * */
    private String path;    //

    /**
     * 经过处理之后的有效参数
     * */
    private Object value;   //

    /**
     * 原始的参数列表
     * */
    private Map<String, Object> argMap;

    /**
     * 请求头信息
     * */
    private Map<String, String> headerMap;


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

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }
    public void setHeaderMap(Map<String, String> headerMap) {
        this.headerMap = headerMap;
    }
}