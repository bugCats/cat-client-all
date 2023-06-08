package cc.bugcat.catclient.beanInfos;


import java.util.Map;


/**
 * 每次执行客户端方法，发起http请求，都会重新解析方法上的参数，创建新的参数对象实例
 *
 * @see CatMethodInfo#parseArgs(java.lang.Object[])
 * @author bugcat
 * */
public class CatParameter {

    /**
     * 真实url，PathVariable已经处理
     * */
    private String realPath;

    /**
     * 请求头信息
     * */
    private Map<String, String> headerMap;

    /**
     * 原始的参数列表
     * */
    private Map<String, Object> argsMap;

    /**
     * 方法上经过处理之后的有效参数
     *
     * 当参数被@RequestBody、@ModelAttribute标记，或者仅当只有一个复杂对象时，value为对象；
     * 其他情况为Map
     * */
    private Object value;



    public String getRealPath() {
        return realPath;
    }
    public void setRealPath(String realPath) {
        this.realPath = realPath;
    }

    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }

    public Map<String, Object> getArgsMap() {
        return argsMap;
    }
    public void setArgsMap(Map<String, Object> argsMap) {
        this.argsMap = argsMap;
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }
    public void setHeaderMap(Map<String, String> headerMap) {
        this.headerMap = headerMap;
    }
}
