package cc.bugcat.catclient.handler;

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
     * 因为有重连机制，所有可能有多组日志
     * */
    private List<CatClientLogger> catLogs = new ArrayList<>();

    /**
     * http请求方式
     * */
    private RequestMethod requestType;
    private boolean postString = false;

    /**
     * 链接超时
     * */
    private int connect;
    private int socket;

    /**
     * url地址：https://blog.csdn.net/qq_41399429
     * */
    private String path;

    /**
     * 请求头信息
     * */
    private Map<String, String> headerMap = new HashMap<>();

    /**
     * 方法上参数列表组成的原始对象
     * */
    private Object objectParam;

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

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }
    public void setHeaderMap(Map<String, String> headerMap) {
        this.headerMap = headerMap;
    }

    public Object getObjectParam() {
        return objectParam;
    }
    public void setObjectParam(Object objectParam) {
        this.objectParam = objectParam;
    }

    public MultiValueMap<String, Object> getKeyValueParam() {
        return keyValueParam;
    }
    public void setKeyValueParam(MultiValueMap<String, Object> keyValueParam) {
        this.keyValueParam = keyValueParam;
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
