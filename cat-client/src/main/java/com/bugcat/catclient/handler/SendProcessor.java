package com.bugcat.catclient.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bugcat.catclient.beanInfos.CatMethodInfo;
import com.bugcat.catclient.beanInfos.CatParameter;
import com.bugcat.catclient.spi.CatClientFactory;
import com.bugcat.catclient.spi.CatJsonResolver;
import com.bugcat.catface.handler.Stringable;
import com.bugcat.catclient.utils.CatClientUtil;
import com.bugcat.catface.utils.CatToosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * http 请求发送类
 * 多例
 * @author bugcat
 * */
public class SendProcessor {

    
    protected static Logger logger = LoggerFactory.getLogger(SendProcessor.class);
    
    private CatMethodInfo methodInfo;
    private CatClientFactory clientFactory;
    
    private String methodName;  //api方法名
    private RequestMethod requestType;
    private boolean postString = false;

    protected int connect;
    protected int socket;

    protected JSONObject notes;    //其他自定义参数、标记

    protected String path;        //url地址     eg： https://blog.csdn.net/qq_41399429

    protected Map<String, String> headerMap = new HashMap<>();  //  请求头信息
    protected Map<String, Object> keyValueParam;  //键值对

    protected String reqStr;      //请求参数
    protected String respStr;     //响应参数

    
    /**
     * 1、设置公共参数
     * */
    public void setConfigInfo(CatMethodInfo methodInfo, CatParameter param){
        
        this.methodInfo = methodInfo;
        this.clientFactory = methodInfo.getClientFactory();
        
        this.methodName = methodInfo.getMethodName();
        this.postString = methodInfo.isPostString();
        
        this.path = methodInfo.getHost() + param.getPath();

        this.notes = new JSONObject();
        methodInfo.getNotes().forEach((key, value) -> {
            if ( value != null && value instanceof String ) {
                Matcher matcher = CatClientUtil.keyPat2.matcher((String) value);
                if( matcher.find()  ){
                    String argsTmpl = matcher.group(1);
                    int start = argsTmpl.indexOf(".");
                    if( start > -1 ){ //是复杂对象
                        String argName = argsTmpl.substring(0, start);
                        Object argObj = param.getArgMap().get(argName);
                        Expression exp = CatToosUtil.parser.parseExpression(argsTmpl.substring(start + 1));
                        notes.put(key, exp.getValue(argObj));
                    } else {    //简单参数
                        Object argObj = param.getArgMap().get(argsTmpl);
                        notes.put(key, argObj);
                    }
                    return;
                }
            }
            notes.put(key, value);            
        });
        
        this.headerMap.putAll(param.getHeaderMap());
        
        this.requestType = methodInfo.getRequestType();
        this.connect = methodInfo.getConnect();
        this.socket = methodInfo.getSocket();

    }

    /**
     * 2、设置参数，如果在调用远程API，需要额外的签名等，可在此步骤添加
     * */
    public final void pretreatment(CatParameter param){
        
        Object value = methodInfo.getParameterProcess().apply(param.getValue());

        CatJsonResolver resolver = clientFactory.getJsonResolver();

        // 使用post发送字符串
        if( isPostString() ){

            if ( value instanceof String ){
                reqStr = CatToosUtil.toStringIfBlank(value, "");
            } else if ( value instanceof Stringable ){
                reqStr = ((Stringable) value).serialize();
            } else {
                reqStr = resolver.toJsonString(value);
            }

            //使用post、get发送键值对
        } else {
            if( value instanceof String ){
                keyValueParam = new HashMap<>(param.getArgMap());
            } else {// 传入了一个对象，转换成键值对
                keyValueParam = beanToMap(value);
            }

            // 请求入参转换成String，方便记录日志
            if(printInLog(false) || printInLog(true)){
                reqStr = resolver.toJsonString(keyValueParam);
            }
        }
    }
    
    
    
    /**
     * 3、如果在调用远程API，需要额外的签名等，可在此步骤添加
     * */
    public void setSendVariable(CatParameter param){
        
    }
    
    
    
    /**
     * 3、发送http
     * */
    public String httpSend() throws CatHttpException {
        
        Map<String, Object> logInfo = new LinkedHashMap<>();
        logInfo.put("time", System.currentTimeMillis());
        
        boolean hasErr = false;
        respStr = null;
        try {
            switch ( requestType ) {
                case GET:
                    respStr = clientFactory.getCatHttp().doGet(path, keyValueParam, headerMap, socket, connect);
                    break;
                case POST:
                    if( postString ){
                        respStr = clientFactory.getCatHttp().jsonPost(path, reqStr, headerMap, socket, connect);
                    } else {
                        respStr = clientFactory.getCatHttp().doPost(path, keyValueParam, headerMap, socket, connect);
                    }
                    break;
            }
        } catch ( CatHttpException ex ) {

            logInfo.put("err", ex.getStatus() != null ? String.valueOf(ex.getStatus()) : ex.getMessage());
            hasErr = true;
            
            throw ex;
            
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
     * 多次请求，共用一个SendProcessor情况下，每次执行http之前，需要重置参数
     * */
    public void reset(){
        this.notes = null;
        this.headerMap.clear();  //  请求头信息
        this.path = null;
        this.keyValueParam = null;
        this.reqStr = null;
        this.respStr = null;
    }
    
    
    
    /**
     * 判断是否为 json post
     * */
    public boolean isPostString(){
        return postString;
    }

    
    /**
     * 复杂对象，转form表单形式
     * */
    public Map<String, Object> beanToMap(Object bean){
        if( bean == null ){
            return new HashMap<>();
        }
        Object value = JSON.toJSON(bean);
        Map<String, Object> result = transform(value);
        return result;
    }

    /**
     * 复杂对象，转form表单形式
     * */
    private Map<String, Object> transform(Object value){
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
    
    
    private boolean printInLog(boolean hasErr) {
        return methodInfo.inLog(hasErr);
    }
    
    private boolean printOutLog(boolean hasErr) {
        return methodInfo.outLog(hasErr);
    }

    
    public RequestMethod getRequestType() {
        return requestType;
    }

    public JSONObject getNotes() {
        return notes;
    }

    public String getReqStr () {
        return reqStr;
    }

    public String getRespStr () {
        return respStr;
    }


    
}
