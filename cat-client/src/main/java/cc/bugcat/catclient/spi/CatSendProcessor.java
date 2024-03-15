package cc.bugcat.catclient.spi;

import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.beanInfos.CatMethodInfo;
import cc.bugcat.catclient.beanInfos.CatParameter;
import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.config.CatHttpRetryConfigurer;
import cc.bugcat.catclient.handler.CatClientContextHolder;
import cc.bugcat.catclient.handler.CatClientDepend;
import cc.bugcat.catclient.handler.CatClientFactoryAdapter;
import cc.bugcat.catclient.handler.CatClientLogger;
import cc.bugcat.catclient.exception.CatHttpException;
import cc.bugcat.catclient.handler.CatHttpPoint;
import cc.bugcat.catclient.handler.CatLogsMod;
import cc.bugcat.catclient.handler.CatMethodAopInterceptor;
import cc.bugcat.catface.handler.EnvironmentAdapter;
import cc.bugcat.catface.handler.Stringable;
import cc.bugcat.catface.utils.CatToosUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

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

    private CatClientContextHolder context;
    private CatHttpPoint httpPoint;

    /**
     * 日志追踪id。可以结合ThreadLocal使用
     * */
    protected String tracerId = null;
    /**
     * 其他自定义参数、标记
     * */
    protected JSONObject notes;

    
    /**
     * 1、初始化http相关配置，每次调用interface的方法，仅执行一次
     * */
    public void doConfigurationResolver(CatClientContextHolder context, CatParameter parameter){
        CatClientInfo clientInfo = context.getClientInfo();
        CatMethodInfo methodInfo = context.getMethodInfo();
        CatClientDepend clientDepend = clientInfo.getClientDepend();
        CatClientConfiguration clientConfig = clientDepend.getClientConfig();
        EnvironmentAdapter envAdapter = clientDepend.getEnvironment();
        CatHttpRetryConfigurer retry = clientDepend.getRetryConfigurer();

        this.context = context;
        this.retryCount = retry.isEnable() ? retry.getRetries() : 0;

        httpPoint = newCatHttpPoint();
        httpPoint.setPostString(methodInfo.isPostString()); //是否使用post发送字符流
        httpPoint.setRequestType(methodInfo.getRequestType());
        httpPoint.setConnect(methodInfo.getConnect());
        httpPoint.setSocket(methodInfo.getSocket());

        httpPoint.setHost(methodInfo.getHost());
        httpPoint.setUrl(parameter.getRealPath());
        httpPoint.setHeaderMap(parameter.getHeaderMap());
        httpPoint.setParameter(parameter);

        this.tracerId = CatToosUtil.defaultIfBlank(this.getTracerId(), clientConfig.tracerProvide());
        this.notes = new JSONObject();
        EnvironmentAdapter newAdapter = EnvironmentAdapter.newAdapter(envAdapter, parameter.getArgsMap());
        methodInfo.getNoteMap().forEach((key, value) -> {
            Object render = newAdapter.getProperty(value, Object.class);
            notes.put(key, render);
        });
    }


    /**
     * 2、参数处理。如果要修改请求方式、参数转换，在这步操作。
     * 仅会执行一次
     * */
    public void doVariableResolver(CatClientContextHolder context) throws Exception {
        
        CatMethodInfo methodInfo = context.getMethodInfo();
        CatParameter parameter = httpPoint.getParameter();
        
        if( methodInfo.isCatface() ){ // 精简模式，全部默认使用post+字符流模式
            CatPayloadResolver jsonResolver = context.getFactoryAdapter().getPayloadResolver();
            Object value = parameter.getValue();
            if( value instanceof Map ){ // 如果方法上入参是键值对，转换成json字符串
                parameter.setValue(jsonResolver.toSendString(value));
            } else { // 如果方法上仅有一个入参，虚拟一个json属性arg0
                Map<String, Object> map = new HashMap<>();
                map.put("arg0", value);
                parameter.setValue(jsonResolver.toSendString(map));
            }
        }
        
        MultiValueMap<String, Object> keyValueParam = null;
        String requestBody = null;
        if( httpPoint.isPostString() ){ 
            
            // 使用post发送字符串
            requestBody = parameterToString(httpPoint);
            
        } else {
            
            // 使用表单方式
            keyValueParam = parameterToMap(httpPoint);
            
            // 请求入参转换成String，方便记录日志
            requestBody = mapToString(keyValueParam);
        }

        httpPoint.setRequestBody(requestBody);
        httpPoint.setKeyValueParam(keyValueParam);
        
    }

    /**
     * 3、如果在调用远程API，需要额外处理参数、添加签名等：
     *   a、继承CatSendProcessor，重写postVariableResolver方法；
     *   b、通过{@link CatSendInterceptor}，在doVariableResolver前后修改；
     * 仅会执行一次
     * */
    public void postVariableResolver(CatClientContextHolder context) {
        
    }

    
    /**
     * 4、发送http
     * 由于有重连机制，每次调用interface的方法，postHttpSend可能会执行多次
     * */
    public String postHttpSend() throws CatHttpException {

        long start = System.currentTimeMillis();

        CatClientInfo clientInfo = context.getClientInfo();
        CatMethodInfo methodInfo = context.getMethodInfo();
        CatClientFactoryAdapter factoryAdapter = context.getFactoryAdapter();

        CatClientLogger catLog = new CatClientLogger();
        catLog.setTracerId(this.getTracerId());
        catLog.setLogsMod(methodInfo.getLogsMod());
        catLog.setClientClass(clientInfo.getClientClass());
        catLog.setLogger(clientInfo.getLogger());
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
    public boolean canRetry(CatClientContextHolder context, CatHttpException exception) {
        CatClientInfo clientInfo = context.getClientInfo();
        CatHttpRetryConfigurer retryConfigurer = clientInfo.getClientDepend().getRetryConfigurer();
        CatMethodInfo methodInfo = context.getMethodInfo();
        if ( retryConfigurer.isEnable() && retryCount > 0) {
            boolean note = retryConfigurer.containsNote(notes);
            boolean tags = retryConfigurer.containsTags(clientInfo.getTagsMap());
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
        return objectResolverSupplier != null ? objectResolverSupplier.get() : new CatObjectResolver.SimpleObjectResolver();
    }

    
    /**
     * 入参Map转字符串
     * */
    protected final String mapToString(Map<String, ?> requestMap) throws Exception {
        if( false == httpPoint.isPostString() ){
            CatMethodInfo methodInfo = context.getMethodInfo();
            // 请求入参转换成String，方便记录日志
            CatLogsMod logsMod = methodInfo.getLogsMod();
            CatPayloadResolver jsonResolver = context.getFactoryAdapter().getPayloadResolver();
            if( logsMod.printIn ){
                return jsonResolver.toSendString(requestMap);
            }
        }
        return null;
    }
    
    /**
     * 入参对象转Map
     * */
    protected final MultiValueMap<String, Object> parameterToMap(CatHttpPoint httpPoint){
        CatParameter parameter = httpPoint.getParameter();
        Object value = parameter.getValue();

        MultiValueMap<String, Object> keyValueParam = null;
        if( value instanceof String ){
            keyValueParam = CatToosUtil.toMultiValueMap(parameter.getArgsMap());
        } else if ( value instanceof Map ) {// 传入了一个对象，转换成键值对
            keyValueParam = CatToosUtil.toMultiValueMap((Map<String, Object>) value);
        } else {// 传入了一个对象，转换成键值对
            CatObjectResolver objectResolver = newCatObjectResolver();
            keyValueParam = objectResolver.resolver(value);
        }
        return keyValueParam;
    }

    /**
     * 入参对象转字符串
     * */
    protected final String parameterToString(CatHttpPoint httpPoint) throws Exception {
        CatParameter parameter = httpPoint.getParameter();
        Object value = parameter.getValue();
        String requestBody = null;
        if ( value instanceof String ){
            requestBody = CatToosUtil.toStringIfBlank(value, "");
        } else if ( value instanceof Stringable ){
            requestBody = ((Stringable) value).serialization();
        } else {
            CatPayloadResolver jsonResolver = context.getFactoryAdapter().getPayloadResolver();
            requestBody = jsonResolver.toSendString(value);
        }
        return requestBody;
    }

    
    
    /**
     * 修改切入点参数类型
     * */
    public final void setHttpPointSupplier(Supplier<CatHttpPoint> httpPointSupplier) {
        this.httpPointSupplier = httpPointSupplier;
    }

    
    /**
     * 修改对象转换成表单的处理器 
     * */
    public final void setObjectResolverSupplier(Supplier<CatObjectResolver> objectResolverSupplier) {
        this.objectResolverSupplier = objectResolverSupplier;
    }
    
    
    /**
     * 切入点参数
     * */
    public CatHttpPoint getHttpPoint() {
        return httpPoint;
    }

    /**
     * 日志id；用于日志追踪，如果没有赋值，则自动生成；
     * */
    public String getTracerId() {
        return tracerId;
    }
    public void setTracerId(String tracerId) {
        this.tracerId = tracerId;
    }
    
    /**
     * 方法上添加的标记
     * */
    public JSONObject getNotes() {
        return notes;
    }


}
