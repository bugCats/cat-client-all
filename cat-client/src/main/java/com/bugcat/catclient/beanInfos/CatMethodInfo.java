package com.bugcat.catclient.beanInfos;


import com.bugcat.catclient.annotation.CatMethod;
import com.bugcat.catclient.annotation.CatNote;
import com.bugcat.catclient.handler.RequestLogs;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.catface.utils.CatToosUtil;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @CatMethod 描述信息
 * 单例
 * @author bugcat
 * */
public class CatMethodInfo {

    
    private String name;    //方法名称
    private String host;    //域名
    private String value;   //调用的url，从@CatMethod注解中获取的原始数据，可以包含${}
    
    private Map<String, Object> notes;   //其他自定义参数、标记
    
    private RequestMethod requestType;  //get|post|delete
    private boolean postJson = false;   //post发送json字符串

    private ShowLog nomalLog = new ShowLog();    // 日志记录方案
    private ShowLog onErrLog = new ShowLog();
    
    private int connect;
    private int socket;

    private Map<String, CatMethodParamInfo> params;             //除了SendProcessor、PathVariable以外，其他的参数map => 参数名:参数对象信息
    private Map<String, CatMethodParamInfo> pathParamIndexMap;  //出现在url上的参数(@PathVariable)map => 参数名:参数对象信息
    
    private CatMethodReturnInfo returnInfo;  //方法返回参数对象
    
    private Integer handlerIndex = null;    //SendProcessor 在参数列表中出现的索引
    
    
    /**
     * 解析方法
     * @param method    
     * @param catClientInfo        @CatClient信息
     * @param prop       加载properties文件
     * @return
     */
    public boolean parse(Method method, CatClientInfo catClientInfo, Properties prop){
        
        StandardMethodMetadata metadata = new StandardMethodMetadata(method);
        AnnotationAttributes attr = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(CatMethod.class.getName()));

        this.name = method.getName();
        
        this.host = catClientInfo.getHost();   // http://www.bugcat.com
        
        this.value = "/" + prop.getProperty(attr.getString("value")).replaceAll("^/", "");

        this.requestType = attr.getEnum("method");  // post | get | jsonp
        
        
        CatNote[] notes = attr.getAnnotationArray("notes", CatNote.class);
        this.notes = new HashMap<>();           //其他自定义参数、标记
        if( notes != null && notes.length > 0 ){
            for ( CatNote note : notes ) {
                String value = CatToosUtil.defaultIfBlank(note.value(), "");
                if( value.startsWith("${") ){
                    this.notes.put(note.key(), prop.getProperty(value));
                } else {
                    this.notes.put(note.key(), value);
                }
            }   
        }

        // 控制日志打印
        RequestLogs logs = RequestLogs.Def == attr.getEnum("logs") ? catClientInfo.getLogs() : attr.getEnum("logs");
        nomalLog.in = logs == RequestLogs.All || logs == RequestLogs.In;
        nomalLog.out = logs == RequestLogs.All || logs == RequestLogs.Out;
        onErrLog.in = logs == RequestLogs.All2  || logs == RequestLogs.In2;
        onErrLog.out = logs == RequestLogs.All2  || logs == RequestLogs.Out2;

        
        int connect = attr.getNumber("connect");
        this.connect = connect < 0 ? -1 : ( connect == 0 ? catClientInfo.getConnect() : connect );  //链接超时

        
        int socket = attr.getNumber("socket");
        this.socket = socket < 0 ? -1 : ( socket == 0 ? catClientInfo.getSocket() : socket );  //链接超时
        

        params = new HashMap<>();   //方法上参数列表，除了SendHandler、PathVariable以外，其他的有效参数
        
        Parameter[] parameters = method.getParameters();
        for ( int i = 0; i < parameters.length; i++ ) {
            
            Parameter parameter = parameters[i];

            //如果post方式
            if( requestType == RequestMethod.POST ){
                //并且参数上有@RequestBody
                if ( parameter.isAnnotationPresent(RequestBody.class) ) {
                    if( postJson ){
                        throw new IllegalArgumentException("方法上只容许出现一个被@RequestBody注解的入参！" + method.toString());
                    } else {
                        postJson = true;
                    }
                }
            }
            
            String pname = null;
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            if( requestParam != null ){
                pname = CatToosUtil.defaultIfBlank(requestParam.value(), requestParam.name());
            } else {
                ModelAttribute model = parameter.getAnnotation(ModelAttribute.class);
                if( model != null ){
                    pname = CatToosUtil.defaultIfBlank(model.value(), model.name());
                } else {
                    pname = parameter.getName();
                }
            }

            Class<?> pclazz = parameter.getType();

            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);    //在url上追加的参数，不绑定到参数列表中
            if( pathVariable != null){
                String pathParam = CatToosUtil.defaultIfBlank(pathVariable.value(), pathVariable.name());
                if(pathParamIndexMap == null){
                    pathParamIndexMap = new HashMap<>();
                }
                pathParamIndexMap.put(pathParam, new CatMethodParamInfo(pname, i, pclazz));
            }
                
            if(SendProcessor.class.isAssignableFrom(parameter.getType())){//这个参数是SendProcessor，不绑定到参数列表中
                handlerIndex = Integer.valueOf(i);
            } else {
                params.put(pname, new CatMethodParamInfo(pname, i, pclazz));
            }
        }

        //方法返回对象
        returnInfo = new CatMethodReturnInfo(method.getReturnType(), method.getGenericReturnType());

        return true;
    }

    
    
    /**
     * 处理入参
     * 
     * 1、将所有的有效入参转成成map => 方法上参数名称：参数值
     * 
     * 2、判断，如果map大小：
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
        if( pathParamIndexMap != null ) {//填充 url 上的参数 
            for ( Map.Entry<String, CatMethodParamInfo> entry : pathParamIndexMap.entrySet() ){
                path = path.replace("{" + entry.getKey() + "}", CatToosUtil.toStringIfBlank(args[entry.getValue().getIndex()], "").toString());
            }
        }
        param.setPath(path);
        
        
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
            if( paramInfo.isSimple() ){//为String、基本数据类型、包装类
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
    public boolean isPostJson() {
        return postJson;
    }

}
