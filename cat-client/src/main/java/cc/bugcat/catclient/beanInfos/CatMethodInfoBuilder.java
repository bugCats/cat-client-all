package cc.bugcat.catclient.beanInfos;

import cc.bugcat.catclient.annotation.CatMethod;
import cc.bugcat.catclient.handler.SendProcessor;
import cc.bugcat.catclient.annotation.CatNote;
import cc.bugcat.catclient.handler.RequestLogs;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.catface.utils.CatToosUtil;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class CatMethodInfoBuilder {

    private final String serviceName;
    private final Method method;
    private final CatClientInfo clientInfo;
    private final Properties prop;

    
    public static CatMethodInfoBuilder builder(Method method, CatClientInfo clientInfo, Properties prop){
        CatMethodInfoBuilder builder = new CatMethodInfoBuilder(method, clientInfo, prop);
        return builder;
    }


    private CatMethodInfoBuilder(Method method, CatClientInfo clientInfo, Properties prop){
        this.serviceName = clientInfo.getServiceName();
        this.method = method;
        this.clientInfo = clientInfo;
        this.prop = prop;
    }
    
    
    /**
     * 方法名称
     * */
    private String methodName;

    /**
     * 域名  eq：http://xxxx，此时${host}已经被变量填充
     * */
    private String host;

    /**
     * 调用的url，从@CatMethod注解中获取的原始数据，可以包含${}
     * */
    private String path;

    /**
     * 发送方式 get|post|delete
     * */
    private RequestMethod requestType;


    /**
     * 是否为精简模式
     * */
    private boolean isCatface = false;
    
    /**
     * 是否为post发送字符串模式
     * */
    private boolean postString;
    
    /**
     * http请求读写超时
     * */
    private int connect;
    private int socket;
    
    /**
     * 方法返回参数对象
     * */
    private CatMethodReturnInfo returnInfo;

    /**
     * SendProcessor 在参数列表中出现的索引
     * 为null，表示需要通过{@link CatClientFactory#getSendHandler()}自动生成
     * */
    private Integer handlerIndex;

    /**
     * 方法上自定义参数、标记
     * */
    private Map<String, Object> notes = new HashMap<>();
    
    /**
     * 除了SendProcessor、PathVariable、RequestHeader以外，其他的参数map => 参数名:参数对象信息
     * */
    private Map<String, CatMethodParamInfo> params = new HashMap<>();

    /**
     * 出现在url上的参数{@link PathVariable}map => 参数名:参数对象信息
     * */
    private Map<String, CatMethodParamInfo> pathParamIndexMap = new HashMap<>();

    /**
     * 出现在url上的参数{@link RequestHeader}map => 参数名:参数对象信息
     * */
    private Map<String, CatMethodParamInfo> headerParamIndexMap = new HashMap<>();
    
    /**
     * 日志记录方案
     * */
    private boolean nomalLogIn = false;
    private boolean nomalLogOut = false;
    private boolean onErrLogIn = false;
    private boolean onErrLogOut = false;
    
    public CatMethodInfo build() {

        AnnotationAttributes attrs = getAttributes(method);
        
        postProcess(attrs);
        
        afterProcess(attrs);
        
        return new CatMethodInfo(this);
    }

    
    private AnnotationAttributes getAttributes(Method method){

        StandardMethodMetadata metadata = new StandardMethodMetadata(method);
        Map<String, Object> map = metadata.getAnnotationAttributes(CatMethod.class.getName());
        
        Catface catface = clientInfo.getCatface();
        if( catface != null ) {//精简模式
            if( map == null ){
                map = new HashMap<>();
                map.put("notes", new CatNote[0]);
                map.put("socket", clientInfo.getSocket());
                map.put("connect", clientInfo.getConnect());
                map.put("logs", clientInfo.getLogs());
            }
            String path = CatToosUtil.getDefaultRequestUrl(catface, serviceName, method);
            map.put("value", path);
            map.put("method", RequestMethod.POST);
            isCatface = true;
        }

        AnnotationAttributes attrs = AnnotationAttributes.fromMap(map);
        return attrs;
        
    }
 
    
    private void postProcess(AnnotationAttributes attrs){
        
        // userSave
        this.methodName = method.getName();

        // http://www.bugcat.com
        this.host = clientInfo.getHost();

        // /user/save
        this.path = "/" + prop.getProperty(attrs.getString("value")).replaceAll("^/", "");

        // post | get
        this.requestType = attrs.getEnum("method");

        // 其他自定义参数、标记
        Map<String, Object> noteMap = new HashMap<>();
        CatNote[] notes = attrs.getAnnotationArray("notes", CatNote.class);
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
        RequestLogs logs = RequestLogs.Def == attrs.getEnum("logs") ? clientInfo.getLogs() : attrs.getEnum("logs");
        this.nomalLogIn = logs == RequestLogs.All || logs == RequestLogs.In;
        this.nomalLogOut = logs == RequestLogs.All || logs == RequestLogs.Out;
        this.onErrLogIn = logs == RequestLogs.All2  || logs == RequestLogs.In2;
        this.onErrLogOut = logs == RequestLogs.All2  || logs == RequestLogs.Out2;

        //链接超时
        int connect = attrs.getNumber("connect");
        this.connect = connect < 0 ? -1 : ( connect == 0 ? clientInfo.getConnect() : connect );

        //链接超时
        int socket = attrs.getNumber("socket");
        this.socket = socket < 0 ? -1 : ( socket == 0 ? clientInfo.getSocket() : socket );

        //方法返回对象
        this.returnInfo = new CatMethodReturnInfo(method.getReturnType(), method.getGenericReturnType());
        
        this.postString = isCatface;    // 如果是精简模式，默认是post+json
        
        //是否已经出现过主要入参对象
        boolean hasPrimary = false;
        boolean isCatface = clientInfo.getCatface() != null;
        
        Parameter[] parameters = method.getParameters();
        for ( int idx = 0; idx < parameters.length; idx++ ) {

            Parameter parameter = parameters[idx];

            //获取参数名称 interface被编译之后，方法上的参数名会被擦除，只能使用注解标记别名
            String pname = null;
            if( isCatface ){
                pname = "arg" + idx;
            } else {
                pname = CatToosUtil.getAnnotationValue(parameter, RequestParam.class, ModelAttribute.class, RequestHeader.class, CatNote.class);
                if( CatToosUtil.isBlank(pname) ){
                    pname = parameter.getName();
                } 
            }

            Class<?> pclazz = parameter.getType();

            //在url上追加的参数，不绑定到参数列表中
            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
            if( pathVariable != null){
                String pathParam = pathVariable.value();
                pathParamIndexMap.put(pathParam, new CatMethodParamInfo(idx, pclazz));
                continue;
            }
            RequestHeader header = parameter.getAnnotation(RequestHeader.class);
            if ( header != null ){
                String pathParam = header.value();
                headerParamIndexMap.put(pathParam, new CatMethodParamInfo(idx, pclazz));
                continue;
            }

            //这个参数是SendProcessor、或者其子类，不绑定到参数列表中
            if(SendProcessor.class.isAssignableFrom(parameter.getType())){
                if( handlerIndex != null ){
                    throw new IllegalArgumentException("方法上只容许出现一个SendProcessor入参！" + method.toString());
                }
                handlerIndex = Integer.valueOf(idx);

            } else {

                CatMethodParamInfo paramInfo = new CatMethodParamInfo(idx, pclazz);

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

    }
    
    
    private void afterProcess(AnnotationAttributes attrs){
 
    }


    
    
    public Method getMethod() {
        return method;
    }
    public Properties getProp() {
        return prop;
    }
    public String getMethodName() {
        return methodName;
    }
    public String getHost() {
        return host;
    }
    public String getPath() {
        return path;
    }
    public RequestMethod getRequestType() {
        return requestType;
    }
    public boolean isCatface() {
        return isCatface;
    }
    public boolean isPostString() {
        return postString;
    }
    public int getConnect() {
        return connect;
    }
    public int getSocket() {
        return socket;
    }
    public CatMethodReturnInfo getReturnInfo() {
        return returnInfo;
    }
    public Integer getHandlerIndex() {
        return handlerIndex;
    }
    public Map<String, Object> getNotes() {
        return notes;
    }
    public Map<String, CatMethodParamInfo> getParams() {
        return params;
    }
    public Map<String, CatMethodParamInfo> getPathParamIndexMap() {
        return pathParamIndexMap;
    }
    public Map<String, CatMethodParamInfo> getHeaderParamIndexMap() {
        return headerParamIndexMap;
    }
    public boolean isNomalLogIn() {
        return nomalLogIn;
    }
    public boolean isNomalLogOut() {
        return nomalLogOut;
    }
    public boolean isOnErrLogIn() {
        return onErrLogIn;
    }
    public boolean isOnErrLogOut() {
        return onErrLogOut;
    }
}
