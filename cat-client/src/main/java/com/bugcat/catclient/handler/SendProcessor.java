package com.bugcat.catclient.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bugcat.catclient.beanInfos.CatMethodInfo;
import com.bugcat.catclient.beanInfos.CatParameter;
import com.bugcat.catclient.spi.CatHttp;
import com.bugcat.catclient.utils.CatToosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;
import java.util.regex.Matcher;


/**
 * http 请求发送类
 * 多例
 * @author bugcat
 * */
public class SendProcessor {

    
    protected static Logger logger = LoggerFactory.getLogger(SendProcessor.class);
    
    private CatMethodInfo methodInfo;
    private CatHttp catHttp;
    
    protected String methodName;  //api方法名
    protected String path;        //url地址     eg： https://blog.csdn.net/qq_41399429
    protected JSONObject notes;    //其他自定义参数、标记
    protected RequestMethod requestType;    //请求方式
    
    
    protected int connect;
    protected int socket;
    
    protected Map<String, String> headerMap = new HashMap<>();  //  请求头信息
    
    protected boolean postJson = false;
    protected Map<String, Object> keyValueParam;  //键值对

    protected String reqStr;      //请求参数
    protected String respStr;     //响应参数


    /**
     * 1、设置公共参数
     * */
    public void setConfigInfo(CatMethodInfo methodInfo, CatParameter param){
        
        this.methodInfo = methodInfo;
        this.catHttp = param.getCatHttp();
        
        this.methodName = methodInfo.getName();
        this.path = methodInfo.getHost() + param.getPath();

        this.notes = new JSONObject();
        for(Map.Entry<String, Object> entry : methodInfo.getNotes().entrySet()){
            Object value = entry.getValue();
            if ( value != null && value instanceof String ) {
                Matcher matcher = CatToosUtil.keyPat2.matcher((String) value);
                if( matcher.find()  ){
                    String argsTmpl = matcher.group(1);
                    int start = argsTmpl.indexOf(".");
                    if( start > -1 ){ //是复杂对象
                        String argName = argsTmpl.substring(0, start);
                        Object argObj = param.getArgMap().get(argName);
                        Expression exp = CatToosUtil.parser.parseExpression(argsTmpl.substring(start + 1));
                        notes.put(entry.getKey(), exp.getValue(argObj));
                    } else {    //简单参数
                        Object argObj = param.getArgMap().get(argsTmpl);
                        notes.put(entry.getKey(), argObj);
                    }
                    continue;
                }
            }
            notes.put(entry.getKey(), value);
        }
        
        
        this.requestType = methodInfo.getRequestType();
        
        this.connect = methodInfo.getConnect();
        this.socket = methodInfo.getSocket();

        this.postJson = methodInfo.isPostJson();
    }

    
    
    /**
     * 2、设置参数，如果在调用远程API，需要额外的签名等，可在此步骤添加
     * */
    public void setSendVariable(CatParameter param){
        
        Object value = param.getValue();
        
        // 使用post发送json字符串
        if( isJsonPost() ){

            reqStr = value instanceof String ? CatToosUtil.toStringIfBlank(value, "") : JSONObject.toJSONString(value);
            
        //使用post、get发送键值对
        } else {
            
            if( value instanceof Map ){
                
                keyValueParam = (Map<String, Object>) value;

            } else {// 传入了一个对象，对象中属性不能有map！！！
                
                keyValueParam = beanToMap(value);
            }
            
            // 请求入参转换成String，方便记录日志
            reqStr = JSONObject.toJSONString(keyValueParam);
        }
    }
    
    
    
    /**
     * 3、发送http
     * */
    public String httpSend() throws Exception{
        
        Map<String, Object> logInfo = new LinkedHashMap<>();
        logInfo.put("time", System.currentTimeMillis());
        
        boolean hasErr = false;
        respStr = null;
        try {
            switch ( requestType ) {
                case GET:
                    respStr = catHttp.doGet(path, keyValueParam, headerMap, socket, connect);
                    break;
                case POST:
                    if( postJson ){
                        respStr = catHttp.jsonPost(path, reqStr, headerMap, socket, connect);
                    } else {
                        respStr = catHttp.doPost(path, keyValueParam, headerMap, socket, connect);
                    }
                    break;
            }
        } catch ( Exception e ) {

            logInfo.put("err", CatToosUtil.defaultIfBlank(e.getMessage(), "null"));
            hasErr = true;
            
            throw e;
            
        } finally {

            logInfo.put("apiName", methodName);
            logInfo.put("apiUrl", path);
            logInfo.put("req", "#{req}");
            logInfo.put("resp", "#{resp}");
            logInfo.put("time", System.currentTimeMillis() - ((Long) logInfo.get("time")));
            
            logger.debug(JSONObject.toJSONString(logInfo)
                    .replace("\"#{req}\"", printInLog(hasErr) ? CatToosUtil.defaultIfBlank(reqStr, "") : "\"\"")
                    .replace("\"#{resp}\"", printOutLog(hasErr) ? CatToosUtil.defaultIfBlank(respStr, "") : "\"\"")
            );
            
        }
        return respStr;
    }
    
    /**
     * 判断是否为 json post
     * */
    public boolean isJsonPost(){
        return postJson;
    }

    
    public Map<String, Object> beanToMap(Object bean){
        if( bean == null ){
            return new HashMap<>();
        }
        Object value = JSON.toJSON(bean);
        return transform(value);
    }
    
    /**
     * 将普通map，转换成form形式
     * */
    protected final Map<String, Object> transform(Object value){
        Map<String, Object> result = new HashMap<>();
        for(Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()){
            transform(result, entry.getKey(), entry.getValue());
        }
        return result;
    }
    private void transform(Map<String, Object> result, String parName, Object value){
        if( value == null ) {
            return;
        }
        if( value instanceof JSONObject ){
            for(Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()){
                transform(result, parName + "." + entry.getKey(), entry.getValue());
            }
        } else if ( value instanceof JSONArray ){
            int count = 0;
            for(Object entry : (List<Object>) value){
                transform(result, parName + "[" + (count ++) + "]", entry);
            }
        } else {
            Object tmp = result.get(parName);
            if( tmp == null ){
                tmp = new LinkedList<>();
                result.put(parName, tmp);
            }
            ((List)tmp).add(value);
        }
    }

    
    
    protected final boolean printInLog(boolean hasErr) {
        return methodInfo.inLog(hasErr);
    }
    protected final boolean printOutLog(boolean hasErr) {
        return methodInfo.outLog(hasErr);
    }


    public String getReqStr () {
        return reqStr;
    }

    public String getRespStr () {
        return respStr;
    }


    
}
