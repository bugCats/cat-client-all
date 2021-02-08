package com.bugcat.catclient.beanInfos;


import com.bugcat.catclient.annotation.CatMethod;
import com.bugcat.catclient.annotation.CatNote;
import com.bugcat.catclient.handler.RequestLogs;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.catclient.spi.CatClientFactory;
import com.bugcat.catface.utils.CatToosUtil;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 方法描述信息，单例
 * {@link CatMethod}
 * 
 * @author bugcat
 * */
public class CatMethodInfo {

    /**
     * 方法名称
     * */
    private final String name;
    
    /**
     * 域名  eq：http://xxxx，此时${host}已经被变量填充
     * */
    private final String host;
    
    /**
     * 调用的url，从@CatMethod注解中获取的原始数据，可以包含${}
     * */
    private final String value;
    
    /**
     * 方法上自定义参数、标记
     * */
    private final Map<String, Object> notes;
    
    /**
     * 发送方式 get|post|delete
     * */
    private final RequestMethod requestType;
    /**
     * 是否为post发送字符串模式
     * */
    private final boolean postString;

    /**
     * 日志记录法案
     * */
    private final ShowLog nomalLog = new ShowLog();
    private final ShowLog onErrLog = new ShowLog();
    
    /**
     * http请求读写超时
     * */
    private final int connect;
    private final int socket;

    /**
     * 除了SendProcessor、PathVariable、RequestHeader以外，其他的参数map => 参数名:参数对象信息
     * */
    private final Map<String, CatMethodParamInfo> params;
    
    /**
     * 出现在url上的参数{@link PathVariable}map => 参数名:参数对象信息
     * */
    private final Map<String, CatMethodParamInfo> pathParamIndexMap;

    /**
     * 出现在url上的参数{@link RequestHeader}map => 参数名:参数对象信息
     * */
    private final Map<String, CatMethodParamInfo> headerParamIndexMap;
    
    /**
     * 方法返回参数对象
     * */
    private final CatMethodReturnInfo returnInfo;
    
    /**
     * SendProcessor 在参数列表中出现的索引
     * 为null，表示需要通过{@link CatClientFactory#getSendHandler()}自动生成
     * */
    private final Integer handlerIndex;
    
    /**
     * 工厂类
     * */
    private CatClientFactory factory;  
    
    
    
    
    
    
    public CatMethodInfo(Method method, CatClientInfo catClientInfo, Properties prop){
        
        
        StandardMethodMetadata metadata = new StandardMethodMetadata(method);
        AnnotationAttributes attr = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(CatMethod.class.getName()));

        // userSave
        this.name = method.getName();

        // http://www.bugcat.com
        this.host = catClientInfo.getHost();   
        
        // /user/save
        this.value = "/" + prop.getProperty(attr.getString("value")).replaceAll("^/", "");

        // post | get
        this.requestType = attr.getEnum("method");

        // 其他自定义参数、标记
        Map<String, Object> noteMap = new HashMap<>();
        CatNote[] notes = attr.getAnnotationArray("notes", CatNote.class);
        if( notes != null && notes.length > 0 ){
            for ( CatNote note : notes ) {
                String value = CatToosUtil.defaultIfBlank(note.value(), "");
                String key = CatToosUtil.isBlank(note.key()) ? value : note.key();  //如果 key属性为空，默认赋值value
                if( value.startsWith("${") ){
                    noteMap.put(key, prop.getProperty(value));   //初步解析 value上的${}变量
                } else {
                    noteMap.put(key, value);
                }
            }   
        }
        this.notes = Collections.unmodifiableMap(noteMap);

        
        // 控制日志打印
        RequestLogs logs = RequestLogs.Def == attr.getEnum("logs") ? catClientInfo.getLogs() : attr.getEnum("logs");
        nomalLog.in = logs == RequestLogs.All || logs == RequestLogs.In;
        nomalLog.out = logs == RequestLogs.All || logs == RequestLogs.Out;
        onErrLog.in = logs == RequestLogs.All2  || logs == RequestLogs.In2;
        onErrLog.out = logs == RequestLogs.All2  || logs == RequestLogs.Out2;

        //链接超时
        int connect = attr.getNumber("connect");
        this.connect = connect < 0 ? -1 : ( connect == 0 ? catClientInfo.getConnect() : connect );

        //链接超时
        int socket = attr.getNumber("socket");
        this.socket = socket < 0 ? -1 : ( socket == 0 ? catClientInfo.getSocket() : socket );  

        //是否为post发送字符串
        boolean postString = false;
        //是否已经出现过主要入参对象
        boolean hasPrimary = false;
        //sendHandler出现的索引值
        Integer handlerIndex = null;
        
        //方法上参数列表，除了SendHandler、PathVariable以外，其他的有效参数
        Map<String, CatMethodParamInfo> params = new HashMap<>();
        
        //出现在url上的参数
        Map<String, CatMethodParamInfo> pathParamIndexMap = new HashMap<>();

        //出现在header上的参数
        Map<String, CatMethodParamInfo> headerParamIndexMap = new HashMap<>();
        
        
        Parameter[] parameters = method.getParameters();
        for ( int i = 0; i < parameters.length; i++ ) {
            
            Parameter parameter = parameters[i];
            
            //获取参数名称 interface被编译之后，方法上的参数名会被擦除，只能使用注解标记别名
            String pname = CatToosUtil.getAnnotationValue(parameter, RequestParam.class, ModelAttribute.class, RequestHeader.class, CatNote.class);
            if( CatToosUtil.isBlank(pname) ){
                pname = parameter.getName();
            }

            Class<?> pclazz = parameter.getType();

            //在url上追加的参数，不绑定到参数列表中
            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
            if( pathVariable != null){
                String pathParam = pathVariable.value();
                pathParamIndexMap.put(pathParam, new CatMethodParamInfo(pname, i, pclazz));
                continue;
            }
            RequestHeader header = parameter.getAnnotation(RequestHeader.class);
            if ( header != null ){
                String pathParam = header.value();
                headerParamIndexMap.put(pathParam, new CatMethodParamInfo(pname, i, pclazz));
                continue;
            }
            
            //这个参数是SendProcessor、或者其子类，不绑定到参数列表中
            if(SendProcessor.class.isAssignableFrom(parameter.getType())){
                if( handlerIndex != null ){
                    throw new IllegalArgumentException("方法上只容许出现一个SendProcessor入参！" + method.toString());
                }
                handlerIndex = Integer.valueOf(i);
                
            } else {

                CatMethodParamInfo paramInfo = new CatMethodParamInfo(pname, i, pclazz);

                if( parameter.isAnnotationPresent(ModelAttribute.class) ||
                        parameter.isAnnotationPresent(RequestBody.class)){
                    
                    if( hasPrimary ){
                        throw new IllegalArgumentException("方法上只容许出现一个被@RequestBody、@ModelAttribute注解的入参！" + method.toString());
                    } else {
                        hasPrimary = true;
                        paramInfo.setPrimary(true);
                    }
                    
                    //如果post方式，并且有@RequestBody注解
                    if( this.requestType == RequestMethod.POST && parameter.isAnnotationPresent(RequestBody.class)){
                        postString = true;
                    }
                }
                
                // 有效参数
                params.put(pname, paramInfo);
            }
        }

        //方法返回对象
        this.returnInfo = new CatMethodReturnInfo(method.getReturnType(), method.getGenericReturnType());

        this.postString = postString;
        this.handlerIndex = handlerIndex;
        this.params = Collections.unmodifiableMap(params);
        this.pathParamIndexMap = Collections.unmodifiableMap(pathParamIndexMap);
        this.headerParamIndexMap = Collections.unmodifiableMap(headerParamIndexMap);
    }

    
    
    /**
     * 处理入参
     * 
     * 1、将所有的有效入参转成成map => 方法上参数名称：参数值
     * 
     * 2、判断map大小：
     * 
     *          为1：再判断该参数是否为基础数据：
     *                  是，直接返回map
     *                  不是，代表是一个对象，返回map的value
     *                  
     *          大于1，直接返回map（入参全部按基础数据处理）
     * 
     * @param args 方法上的入参组成的数组
     *             
     * */
    public CatParameter parseArgs(Object[] args){

        CatParameter param = new CatParameter();
        
        
        //处理url上的参数 =>  /api/{uid}
        String path = value;
        if( pathParamIndexMap.size() > 0 ) {//填充 url 上的参数 
            for ( Map.Entry<String, CatMethodParamInfo> entry : pathParamIndexMap.entrySet() ){
                path = path.replace("{" + entry.getKey() + "}", CatToosUtil.toStringIfBlank(args[entry.getValue().getIndex()], "").toString());
            }
        }
        param.setPath(path);


        // 处理header参数
        Map<String, String> headerMap = new HashMap<>();
        if( headerParamIndexMap.size() > 0 ) {//填充 url 上的参数 
            for ( Map.Entry<String, CatMethodParamInfo> entry : headerParamIndexMap.entrySet() ){
                headerMap.put(entry.getKey(), String.valueOf(args[entry.getValue().getIndex()]));  // entry.getValue().getIndex()=该参数，在方法上出现的索引值
            }
        }        
        param.setHeaderMap(headerMap);
        
        
        // 将入参数组args，转换成： 参数名->入参    此时argMap中一定不包含SendProcessor
        Map<String, Object> argMap = new HashMap<>();
        params.forEach((key, value) -> {
            argMap.put(key, args[value.getIndex()]);  // entry.getValue().getIndex()=该参数，在方法上出现的索引值
        });
        param.setArgMap(argMap);

        
        Object arg = null;
        if( params.size() == 1 ){//如果入参仅一个
            Map.Entry<String, Object> entry = argMap.entrySet().iterator().next();
            CatMethodParamInfo paramInfo = params.get(entry.getKey());
            if( paramInfo.isSimple() && !paramInfo.isPrimary() ){//为String、基本数据类型、包装类、非主要入参类
                arg = argMap;
            } else {//是对象，将value值返回，在下一步再将对象转换成键值对
                arg = argMap.values().iterator().next();
            }
        } else {//入参是多个，转成键值对
            arg = argMap;
        }
        param.setValue(arg);
        
        
        return param;
    }
    
    
    private static class ShowLog {
        private boolean in;
        private boolean out;
    }
    
    
    public boolean inLog(boolean err){
        return err ? onErrLog.in : nomalLog.in; 
    }
    public boolean outLog(boolean err){
        return err ? onErrLog.out : nomalLog.out;
    }
    public Integer getHandlerIndex () {
        return handlerIndex;
    }
    public String getName () {
        return name;
    }
    public String getHost () {
        return host;
    }
    public Map<String, Object> getNotes() {
        return notes;
    }
    public RequestMethod getRequestType () {
        return requestType;
    }
    public int getConnect () {
        return connect;
    }
    public int getSocket () {
        return socket;
    }
    public CatMethodReturnInfo getReturnInfo () {
        return returnInfo;
    }
    public boolean isPostString() {
        return postString;
    }

    public CatClientFactory getFactory() {
        return factory;
    }
    public void setFactory(CatClientFactory factory) {
        this.factory = factory;
    }
}
