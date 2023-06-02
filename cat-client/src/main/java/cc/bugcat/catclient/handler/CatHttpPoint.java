package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.beanInfos.CatParameter;
import cc.bugcat.catface.utils.CatToosUtil;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 当前http请求的相关入参
 *
 * @author bugcat
 * */
public class CatHttpPoint {


    /**
     * 其他额外参数
     * */
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * 因为有重连机制，所有可能有多组日志
     * */
    private List<CatClientLogger> catLogs = new ArrayList<>();

    /**
     * http请求方式
     * */
    private RequestMethod requestType;

    /**
     * 是否使用post发送字符流
     * 一般如果请求方式为POST，并且入参上有@RequestBody，就会被视为post+字符流模式。
     * 但是结合服务端使用，也不排除个别情况
     * */
    private boolean postString = false;

    /**
     * 链接超时
     * */
    private int connect;
    private int socket;

    /**
     * CatClient 配置的主机：https://blog.csdn.net
     * */
    private String host;
    
    /**
     * url地址：/qq_41399429
     * */
    private String url;

    /**
     * 请求头信息
     * */
    private Map<String, String> headerMap = new HashMap<>();

    /**
     * 方法上参数列表组成的原始对象。
     * 每次执行客户端方法，都会创建不同实例
     * */
    private CatParameter parameter;

    /**
     * 键值对
     * 当使用post、get方式发送键值对时，有值
     * */
    private MultiValueMap<String, Object> keyValueParam;
    /**
     * 请求对象序列化
     * 1、如果是使用post+json方式，则为最终入参。
     * 2、如果是使用键值对，则将入参序列化后，用于记录日志
     * */
    private String requestBody;
    
    
    /**
     * http请求原始响应。
     * */
    private String responseBody;





    public Map<String, Object> getAttributes() {
        return attributes;
    }
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void addCatLog(CatClientLogger catLog){
        catLogs.add(catLog);
    }
    public CatClientLogger getLastCatLog(){
        return catLogs.get(catLogs.size() - 1);
    }

    public List<CatClientLogger> getCatLogs() {
        return catLogs;
    }

    public RequestMethod getRequestType() {
        return requestType;
    }
    public void setRequestType(RequestMethod requestType) {
        this.requestType = requestType;
    }

    public boolean isPostString() {
        return postString;
    }
    public void setPostString(boolean postString) {
        this.postString = postString;
    }

    public int getConnect() {
        return connect;
    }
    public void setConnect(int connect) {
        this.connect = connect;
    }

    public int getSocket() {
        return socket;
    }
    public void setSocket(int socket) {
        this.socket = socket;
    }

    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }
    public void setHeaderMap(Map<String, String> headerMap) {
        this.headerMap = headerMap;
    }

    public CatParameter getParameter() {
        return parameter;
    }
    public void setParameter(CatParameter parameter) {
        this.parameter = parameter;
    }

    public MultiValueMap<String, Object> getKeyValueParam() {
        return keyValueParam;
    }
    public void setKeyValueParam(MultiValueMap<String, Object> keyValueParam) {
        this.keyValueParam = keyValueParam;
    }
    public void setKeyValueParam(Map<String, Object> keyValueParam) {
        this.keyValueParam = CatToosUtil.toMultiValueMap(keyValueParam);
    }

    public String getRequestBody() {
        return requestBody;
    }
    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }
    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }


}
