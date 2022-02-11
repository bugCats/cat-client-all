package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.beanInfos.CatMethodInfo;
import cc.bugcat.catclient.beanInfos.CatParameter;
import cc.bugcat.catclient.config.CatHttpRetryConfigurer;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatJsonResolver;
import cc.bugcat.catclient.utils.CatClientUtil;
import cc.bugcat.catface.handler.Stringable;
import cc.bugcat.catface.utils.CatToosUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.expression.Expression;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * http 请求发送类
 *
 * 被{@code @CatMethod}标记的方法，执行http时的核心处理类，必须为多例。
 *
 * 可通过{@link CatClientFactory#newSendHandler()}自动创建，也支持手动创建
 *
 * @author bugcat
 * */
public class CatSendProcessor {

    /**
     * http异常重试次数
     * */
    private int retryCount = 0;

    private CatSendContextHolder context;

    private CatHttpPoint httpPoint;

    protected JSONObject notes;    //其他自定义参数、标记

    /**
     * 1、初始化http相关配置，每次调用interface的方法，仅执行一次
     * */
    public final void sendConfigurationResolver(CatSendContextHolder context, CatParameter parameter){
        this.context = context;
        this.retryCount = context.getRetryConfigurer() != null && context.getRetryConfigurer().isEnable() ? context.getRetryConfigurer().getRetries() : 0;

        CatMethodInfo methodInfo = context.getMethodInfo();

        httpPoint = newCatHttpPoint();
        httpPoint.setPostString(methodInfo.isPostString());
        httpPoint.setRequestType(methodInfo.getRequestType());
        httpPoint.setConnect(methodInfo.getConnect());
        httpPoint.setSocket(methodInfo.getSocket());

        httpPoint.setPath(methodInfo.getHost() + parameter.getPath());
        httpPoint.setHeaderMap(parameter.getHeaderMap());

        this.notes = new JSONObject();
        for ( Map.Entry<String, Object> entry : methodInfo.getNotes().entrySet() ) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if ( value != null && value instanceof String ) {
                Matcher matcher = CatClientUtil.keyPat2.matcher((String) value);
                if( matcher.find()  ){
                    String argsTmpl = matcher.group(1);
                    int start = argsTmpl.indexOf(".");
                    if( start > -1 ){ //是复杂对象
                        String argName = argsTmpl.substring(0, start);
                        Object argObj = parameter.getArgMap().get(argName);
                        Expression exp = CatToosUtil.parser.parseExpression(argsTmpl.substring(start + 1));
                        notes.put(key, exp.getValue(argObj));
                    } else {    //简单参数
                        Object argObj = parameter.getArgMap().get(argsTmpl);
                        notes.put(key, argObj);
                    }
                    return;
                }
            }
            notes.put(key, value);
        }

        this.postVariableResolver(context, parameter, httpPoint);
    }


    /**
     * 2、如果在调用远程API，需要额外处理参数、添加签名等，可在此步骤添加
     * */
    protected void postVariableResolver(CatSendContextHolder context, CatParameter parameter, CatHttpPoint httpPoint){

        CatJsonResolver resolver = context.getClientFactory().getJsonResolver();
        CatMethodInfo methodInfo = context.getMethodInfo();

        Object value = parameter.getValue();
        if( methodInfo.isCatface() ){
            if( value instanceof Map ){
                value = resolver.toJsonString(value);
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("arg0", value);
                value = resolver.toJsonString(map);
            }
        }

        String reqStr = null;
        Map<String, Object> keyValueParam = null;

        // 使用post发送字符串
        if( methodInfo.isPostString() ){
            if ( value instanceof String ){
                reqStr = CatToosUtil.toStringIfBlank(value, "");
            } else if ( value instanceof Stringable ){
                reqStr = ((Stringable) value).serialization();
            } else {
                reqStr = resolver.toJsonString(value);
            }

        } else {    //使用post、get发送键值对
            if( value instanceof String ){
                keyValueParam = new HashMap<>(parameter.getArgMap());
            } else {// 传入了一个对象，转换成键值对
                keyValueParam = beanToMap(value);
            }
            // 请求入参转换成String，方便记录日志
            CatLogsMod logsMod = methodInfo.getLogsMod();
            if( CatLogsMod.All == logsMod || CatLogsMod.All2 == logsMod || CatLogsMod.In == logsMod || CatLogsMod.In2 == logsMod ){
                reqStr = resolver.toJsonString(keyValueParam);
            }
        }
        httpPoint.setRequestBody(reqStr);
        httpPoint.setKeyValueParam(keyValueParam);

        this.afterVariableResolver(context, parameter, httpPoint);
    }


    /**
     * 3、对参数额外处理
     * */
    protected void afterVariableResolver(CatSendContextHolder context, CatParameter parameter, CatHttpPoint httpPoint){

    }



    /**
     * 4、发送http
     * 由于有重连机制，每次调用interface的方法，httpSend可能执行多次
     * */
    public final String httpSend() throws CatHttpException {

        long start = System.currentTimeMillis();

        CatMethodInfo methodInfo = context.getMethodInfo();
        CatClientFactory clientFactory = context.getClientFactory();

        CatClientLogger catLog = new CatClientLogger();
        catLog.setLogsMod(methodInfo.getLogsMod());
        catLog.setApiName(methodInfo.getMethodName());
        catLog.setApiUrl(httpPoint.getPath());
        catLog.setRequest(httpPoint.getRequestBody());

        String respStr = null;
        try {
            respStr = clientFactory.getCatHttp().doHttp(httpPoint, catLog);
        } catch ( CatHttpException ex ) {
            catLog.setException(ex);
            throw ex;
        } finally {
            long end = System.currentTimeMillis();
            catLog.setResponse(respStr);
            catLog.setExecuteTime(end - start);
            httpPoint.addCatLog(catLog);
        }
        return respStr;
    }


    /**
     * 如果发生http异常，判断是否满足重连
     * */
    public boolean canRetry(CatSendContextHolder context, CatHttpException exception) {
        CatHttpRetryConfigurer retryConfigurer = context.getRetryConfigurer();
        CatClientInfo clientInfo = context.getClientInfo();
        CatMethodInfo methodInfo = context.getMethodInfo();
        if ( retryConfigurer.isEnable() && retryCount > 0) {
            boolean note = retryConfigurer.containsNote(notes);
            boolean tags = retryConfigurer.containsTags(clientInfo.getTags());
            boolean method = retryConfigurer.containsMethod(methodInfo.getMethodName());
            boolean status = retryConfigurer.containsStatus(exception.getStatusCode());
            boolean ex = retryConfigurer.containsException(exception.getIntrospectedClass());
            if( note || (tags && method && ( status || ex )) ){
                retryCount = retryCount - 1; //重连次数减一
                return true;
            }
        }
        return false;
    }




    /**
     * 当前http请求的相关入参
     * 子类可重写增强
     * */
    protected CatHttpPoint newCatHttpPoint(){
        return new CatHttpPoint();
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
    protected Map<String, Object> transform(Object value){
        Map<String, Object> result = new HashMap<>();
        for(Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()){
            transform(result, entry.getKey(), entry.getValue());
        }
        return result;
    }
    protected void transform(Map<String, Object> result, String parName, Object value){
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



    public JSONObject getNotes() {
        return notes;
    }
    public CatHttpPoint getHttpPoint() {
        return httpPoint;
    }


}
