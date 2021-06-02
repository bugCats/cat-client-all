package com.bugcat.catclient.beanInfos;


import com.bugcat.catclient.annotation.CatMethod;
import com.bugcat.catclient.spi.CatClientFactory;
import com.bugcat.catclient.spi.CatJsonResolver;
import com.bugcat.catface.utils.CatToosUtil;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

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
    private final String methodName;

    /**
     * 域名  eq：http://xxxx，此时${host}已经被变量填充
     * */
    private final String host;

    /**
     * 调用的url，从@CatMethod注解中获取的原始数据，可以包含${}
     * */
    private final String path;

    /**
     * 方法上自定义参数、标记
     * */
    private final Map<String, Object> notes;

    /**
     * 发送方式 get|post|delete
     * */
    private final RequestMethod requestType;

    /**
     * 是否为精简模式
     * */
    private final boolean isCatface;

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
    private CatClientFactory clientFactory;



    protected CatMethodInfo(CatMethodInfoBuilder builder){
        this.methodName = builder.getMethodName();
        this.host = builder.getHost();
        this.path = builder.getPath();

        this.requestType = builder.getRequestType();
        this.postString = builder.isPostString();

        this.nomalLog.in = builder.isNomalLogIn();
        this.nomalLog.out = builder.isNomalLogOut();
        this.onErrLog.in = builder.isOnErrLogIn();
        this.onErrLog.out = builder.isOnErrLogOut();

        this.connect = builder.getConnect();
        this.socket = builder.getSocket();

        this.returnInfo = builder.getReturnInfo();
        this.handlerIndex = builder.getHandlerIndex();
        this.isCatface = builder.isCatface();
        
        this.notes = Collections.unmodifiableMap(builder.getNotes());
        this.params = Collections.unmodifiableMap(builder.getParams());
        this.pathParamIndexMap = Collections.unmodifiableMap(builder.getPathParamIndexMap());
        this.headerParamIndexMap = Collections.unmodifiableMap(builder.getHeaderParamIndexMap());
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
        String path = this.path;
        if( pathParamIndexMap.size() > 0 ) {//填充 url 上的参数 
            for ( Map.Entry<String, CatMethodParamInfo> entry : pathParamIndexMap.entrySet() ){
                path = path.replace("{" + entry.getKey() + "}", CatToosUtil.toStringIfBlank(args[entry.getValue().getIndex()], "").toString());
            }
            path = path.replaceAll("/+", "/");
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
            //被@RequestBody、@ModelAttribute标记了、或者是复杂对象，直接取对象，再转换
            if( paramInfo.isPrimary() || !paramInfo.isSimple()){    
                arg = argMap.values().iterator().next();
            } else {
                arg = argMap;
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
    public String getMethodName() {
        return methodName;
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
    public boolean isCatface() {
        return isCatface;
    }
    public boolean isPostString() {
        return postString;
    }
    public CatClientFactory getClientFactory() {
        return clientFactory;
    }
    public void setClientFactory(CatClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }
}
