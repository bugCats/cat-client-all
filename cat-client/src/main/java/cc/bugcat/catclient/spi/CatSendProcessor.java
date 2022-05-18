package cc.bugcat.catclient.spi;

import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.beanInfos.CatMethodInfo;
import cc.bugcat.catclient.beanInfos.CatParameter;
import cc.bugcat.catclient.config.CatHttpRetryConfigurer;
import cc.bugcat.catclient.handler.*;
import cc.bugcat.catclient.utils.CatClientUtil;
import cc.bugcat.catface.handler.Stringable;
import cc.bugcat.catface.utils.CatToosUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.expression.Expression;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;

/**
 * http 请求发送类
 *
 * 被{@code @CatMethod}标记的方法，执行http时的核心处理类，必须为多例。
 *
 * 可通过{@link CatClientFactory#newSendHandler()}自动创建，也支持手动创建
 *
 * @see CatMethodAopInterceptor
 * @author bugcat
 * */
public class CatSendProcessor {

    /**
     * http异常重试次数
     * */
    private int retryCount = 0;
    
    private Supplier<CatHttpPoint> httpPointSupplier;
    private Supplier<CatObjectResolver> objectResolverSupplier;

    private CatSendContextHolder context;
    private CatHttpPoint httpPoint;

    /**
     * 其他自定义参数、标记
     * */
    protected JSONObject notes;

    
    /**
     * 1、初始化http相关配置，每次调用interface的方法，仅执行一次
     * */
    public void postConfigurationResolver(CatSendContextHolder context, CatParameter parameter){
        CatMethodInfo methodInfo = context.getMethodInfo();
        
        this.context = context;
        this.retryCount = context.getRetryConfigurer().isEnable() ? context.getRetryConfigurer().getRetries() : 0;

        httpPoint = newCatHttpPoint();
        httpPoint.setPostString(methodInfo.isPostString()); //是否使用post发送字符流
        httpPoint.setRequestType(methodInfo.getRequestType());
        httpPoint.setConnect(methodInfo.getConnect());
        httpPoint.setSocket(methodInfo.getSocket());

        httpPoint.setHost(methodInfo.getHost());
        httpPoint.setUrl(parameter.getRealPath());
        httpPoint.setHeaderMap(parameter.getHeaderMap());
        httpPoint.setParameter(parameter);
        
        this.notes = new JSONObject();
        for ( Map.Entry<String, Object> entry : methodInfo.getNotes().entrySet() ) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if ( value != null && value instanceof String ) {
                Matcher matcher = CatClientUtil.PARAM_KEY_PAT.matcher((String) value);
                if( matcher.find()  ){
                    String argsTmpl = matcher.group(1);
                    int start = argsTmpl.indexOf(".");
                    if( start > -1 ){ //是复杂对象
                        String argName = argsTmpl.substring(0, start);
                        Object argObj = parameter.getArgMap().get(argName);
                        Expression exp = CatToosUtil.parser.parseExpression(argsTmpl.substring(start + 1));
                        notes.put(key, exp.getValue(argObj));
                        continue;
                    } else {    //简单参数
                        Object argObj = parameter.getArgMap().get(argsTmpl);
                        notes.put(key, argObj);
                        continue;
                    }
                }
            }
            notes.put(key, value);
        }
    }


    /**
     * 2、参数预处理
     * 仅会执行一次
     * */
    public void preVariableResolver(CatSendContextHolder context){
        CatMethodInfo methodInfo = context.getMethodInfo();
        CatParameter parameter = httpPoint.getParameter();
        Object value = parameter.getValue();
        CatJsonResolver jsonResolver = context.getFactoryAdapter().getJsonResolver();
        if( methodInfo.isCatface() ){ // 精简模式，全部默认使用post+字符流模式
            if( value instanceof Map ){ // 如果方法上入参是键值对，转换成json字符串
                value = jsonResolver.toSendString(value);
            } else { // 如果方法上仅有一个入参，虚拟一个json属性arg0
                Map<String, Object> map = new HashMap<>();
                map.put("arg0", value);
                value = jsonResolver.toSendString(map);
            }
        }
        
        MultiValueMap<String, Object> keyValueParam = null;
        String requestBody = null;
        if( httpPoint.isPostString() ){ // 使用post发送字符串
            if ( value instanceof String ){
                requestBody = CatToosUtil.toStringIfBlank(value, "");
            } else if ( value instanceof Stringable ){
                requestBody = ((Stringable) value).serialization();
            } else {
                requestBody = jsonResolver.toSendString(value);
            }
        } else { // 使用表单方式
            if( value instanceof String ){
                keyValueParam = CatToosUtil.toMultiValueMap(parameter.getArgMap());
            } else if ( value instanceof Map ) {// 传入了一个对象，转换成键值对
                keyValueParam = CatToosUtil.toMultiValueMap((Map<String, Object>) value);
            } else {// 传入了一个对象，转换成键值对
                CatObjectResolver objectResolver = newCatObjectResolver();
                keyValueParam = objectResolver.resolver(value);
            }

            // 请求入参转换成String，方便记录日志
            CatLogsMod logsMod = methodInfo.getLogsMod();
            if( logsMod.printIn ){
                requestBody = jsonResolver.toSendString(keyValueParam);
            }
        }

        httpPoint.setRequestBody(requestBody);
        httpPoint.setKeyValueParam(keyValueParam);

    }

    /**
     * 3、如果在调用远程API，需要额外处理参数、添加签名等：
     *   a、继承CatSendProcessor，重写afterVariableResolver方法；
     *   b、通过{@link CatMethodSendInterceptor}，在preVariableResolver前后修改；
     * 仅会执行一次
     * */
    public void postVariableResolver(CatSendContextHolder context) {
        
    }

    
    /**
     * 4、发送http
     * 由于有重连机制，每次调用interface的方法，postHttpSend可能会执行多次
     * */
    public String postHttpSend() throws CatHttpException {

        long start = System.currentTimeMillis();

        CatMethodInfo methodInfo = context.getMethodInfo();
        CatClientFactoryAdapter factoryAdapter = context.getFactoryAdapter();

        CatClientLogger catLog = new CatClientLogger();
        catLog.setUuid(context.getUuid());
        catLog.setLogsMod(methodInfo.getLogsMod());
        catLog.setApiName(methodInfo.getMethodName());
        catLog.setApiUrl(httpPoint.getHost() + httpPoint.getUrl());
        catLog.setRequest(httpPoint.getRequestBody());

        String respStr = null;
        try {
            respStr = factoryAdapter.getCatHttp().doHttp(httpPoint, catLog);
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
     * 5、如果发生http异常，判断是否满足重连
     * */
    public boolean canRetry(CatSendContextHolder context, CatHttpException exception) {
        CatHttpRetryConfigurer retryConfigurer = context.getRetryConfigurer();
        CatClientInfo clientInfo = context.getClientInfo();
        CatMethodInfo methodInfo = context.getMethodInfo();
        if ( retryConfigurer.isEnable() && retryCount > 0) {
            boolean note = retryConfigurer.containsNote(notes);
            boolean tags = retryConfigurer.containsTags(clientInfo.getTagMap());
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
     * 当前http请求的相切入点参数
     * 子类可重写增强
     * */
    protected CatHttpPoint newCatHttpPoint(){
        return httpPointSupplier != null ? httpPointSupplier.get() : new CatHttpPoint();
    }

    /**
     * 键值对模式下，复杂对象转换
     * 子类可重写增强
     * */
    protected CatObjectResolver newCatObjectResolver(){
        return objectResolverSupplier != null ? objectResolverSupplier.get() : new CatObjectResolver.DefaultResolver();
    }

    
    
    /**
     * 修改切入点参数类型
     * */
    public void setHttpPointSupplier(Supplier<CatHttpPoint> httpPointSupplier) {
        this.httpPointSupplier = httpPointSupplier;
    }

    
    /**
     * 修改对象转换成表单的处理器 
     * */
    public void setObjectResolverSupplier(Supplier<CatObjectResolver> objectResolverSupplier) {
        this.objectResolverSupplier = objectResolverSupplier;
    }
    
    /**
     * 切入点参数
     * */
    public CatHttpPoint getHttpPoint() {
        return httpPoint;
    }
    
    /**
     * 方法上添加的标记
     * */
    public JSONObject getNotes() {
        return notes;
    }


}
