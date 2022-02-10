package cc.bugcat.catclient.handler;

import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * 当前http请求的相关入参
 *
 * @author bugcat
 * */
public class CatHttpPoint {

    private RequestMethod requestType;
    private boolean postString = false;

    private int connect;
    private int socket;

    private String path;        //url地址     eg： https://blog.csdn.net/qq_41399429

    private Map<String, String> headerMap = new HashMap<>();  //  请求头信息
    private Map<String, Object> keyValueParam;  //键值对
    private String requestBody;      //请求参数

    private String responseBody;





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

    public Map<String, Object> getKeyValueParam() {
        return keyValueParam;
    }
    public void setKeyValueParam(Map<String, Object> keyValueParam) {
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
