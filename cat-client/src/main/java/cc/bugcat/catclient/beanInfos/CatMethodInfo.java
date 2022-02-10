package cc.bugcat.catclient.beanInfos;


import cc.bugcat.catclient.annotation.CatMethod;
import cc.bugcat.catclient.annotation.CatNote;
import cc.bugcat.catclient.handler.CatSendProcessor;
import cc.bugcat.catclient.handler.CatLogsMod;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.catface.utils.CatToosUtil;
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
 * http请求方法描述信息
 * {@link CatMethod}
 *
 * @author bugcat
 * */
public class CatMethodInfo {

    /**
     * 方法名称
     */
    private final String methodName;

    /**
     * 域名：http://${host}/ctx
     * 此时${host}已经被变量填充
     */
    private final String host;

    /**
     * 调用的url
     * 从@CatMethod注解中获取的原始数据，可以包含{PathVariable}参数
     */
    private final String path;

    /**
     * 方法上自定义参数、标记
     */
    private final Map<String, Object> notes;

    /**
     * 发送方式 get|post|delete
     */
    private final RequestMethod requestType;

    /**
     * 是否为精简模式
     */
    private final boolean isCatface;

    /**
     * 是否为post发送字符串模式
     */
    private final boolean postString;

    /**
     * 日志记录方案
     */
    private CatLogsMod logsMod;

    /**
     * http请求读写超时
     */
    private final int connect;
    private final int socket;

    /**
     * 除了SendProcessor、PathVariable、RequestHeader以外，其他的参数map => 参数名:参数对象信息
     */
    private final Map<String, CatMethodParamInfo> params;

    /**
     * 出现在url上的参数{@link PathVariable}map => 参数名:参数对象信息
     */
    private final Map<String, CatMethodParamInfo> pathParamIndexMap;

    /**
     * 出现在url上的参数{@link RequestHeader}map => 参数名:参数对象信息
     */
    private final Map<String, CatMethodParamInfo> headerParamIndexMap;

    /**
     * 方法返回参数对象
     */
    private final CatMethodReturnInfo returnInfo;

    /**
     * SendProcessor 在参数列表中出现的索引
     * 为null，表示需要通过{@link CatClientFactory#newSendHandler()}自动生成
     */
    private final Integer handlerIndex;


    private CatMethodInfo(CatMethodInfoBuilder builder) {
        this.methodName = builder.methodName;
        this.host = builder.host;
        this.path = builder.path;

        this.requestType = builder.requestType;
        this.postString = builder.postString;
        this.logsMod = builder.logsMod;

        this.connect = builder.connect;
        this.socket = builder.socket;

        this.returnInfo = builder.returnInfo;
        this.handlerIndex = builder.handlerIndex;
        this.isCatface = builder.isCatface;

        this.notes = Collections.unmodifiableMap(builder.notes);
        this.params = Collections.unmodifiableMap(builder.params);
        this.pathParamIndexMap = Collections.unmodifiableMap(builder.pathParamIndexMap);
        this.headerParamIndexMap = Collections.unmodifiableMap(builder.headerParamIndexMap);
    }




    /**
     * 处理入参
     *      1、将所有的有效入参转成map => 方法上参数名称：参数值
     *      2、判断map大小：
     *
     *      为1：再判断该参数是否为基础数据：
     *          是，直接返回map
     *          不是，代表是一个对象，返回map的value
     *
     *      大于1，直接返回map（入参全部按基础数据处理）
     *
     * @param args 方法上的入参组成的数组
     */
    public CatParameter parseArgs(Object[] args) {

        CatParameter param = new CatParameter();

        //处理url上的参数 =>  /api/{uid}
        String path = this.path;
        if ( pathParamIndexMap.size() > 0 ) {//填充 url 上的参数
            for ( Map.Entry<String, CatMethodParamInfo> entry : pathParamIndexMap.entrySet() ) {
                path = path.replace("{" + entry.getKey() + "}", CatToosUtil.toStringIfBlank(args[entry.getValue().getIndex()], ""));
            }
            path = path.replaceAll("/+", "/");
        }
        param.setPath(path);


        // 处理header参数
        Map<String, String> headerMap = new HashMap<>();
        if ( headerParamIndexMap.size() > 0 ) {//填充 url 上的参数
            for ( Map.Entry<String, CatMethodParamInfo> entry : headerParamIndexMap.entrySet() ) {
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
        if ( params.size() == 1 ) {//如果入参仅一个
            Map.Entry<String, Object> entry = argMap.entrySet().iterator().next();
            CatMethodParamInfo paramInfo = params.get(entry.getKey());
            //被@RequestBody、@ModelAttribute标记了、或者是复杂对象，直接取对象，再转换
            if ( paramInfo.isPrimary() || !paramInfo.isSimple() ) {
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



    public CatLogsMod getLogsMod() {
        return logsMod;
    }
    public Integer getHandlerIndex() {
        return handlerIndex;
    }
    public String getMethodName() {
        return methodName;
    }
    public String getHost() {
        return host;
    }
    public Map<String, Object> getNotes() {
        return notes;
    }
    public RequestMethod getRequestType() {
        return requestType;
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
    public boolean isCatface() {
        return isCatface;
    }
    public boolean isPostString() {
        return postString;
    }




    /**********************************************************************************************************************/



    public static CatMethodInfoBuilder builder(Method method, CatClientInfo clientInfo, Properties envProp) {
        CatMethodInfoBuilder builder = new CatMethodInfoBuilder(method, clientInfo, envProp);
        return builder;
    }


    public static class CatMethodInfoBuilder {

        private final String serviceName;
        private final Method method;
        private final CatClientInfo clientInfo;
        private final Properties envProp;

        private CatMethodInfoBuilder(Method method, CatClientInfo clientInfo, Properties envProp) {
            this.serviceName = clientInfo.getServiceName();
            this.method = method;
            this.clientInfo = clientInfo;
            this.envProp = envProp;
        }

        /**
         * 方法名称
         */
        private String methodName;

        /**
         * 域名  eq：http://xxxx，此时${host}已经被变量填充
         */
        private String host;

        /**
         * 调用的url，从@CatMethod注解中获取的原始数据，可以包含{PathVariable}
         */
        private String path;

        /**
         * 发送方式 get|post|delete
         */
        private RequestMethod requestType;


        /**
         * 是否为精简模式
         */
        private boolean isCatface = false;

        /**
         * 是否为post发送字符串模式
         */
        private boolean postString;

        /**
         * http请求读写超时
         */
        private int connect;
        private int socket;

        /**
         * 日志记录方案
         */
        private CatLogsMod logsMod;

        /**
         * 方法返回参数对象
         */
        private CatMethodReturnInfo returnInfo;

        /**
         * SendProcessor 在参数列表中出现的索引
         * 为null，表示需要通过{@link CatClientFactory#newSendHandler()}自动生成
         */
        private Integer handlerIndex;

        /**
         * 方法上自定义参数、标记
         */
        private Map<String, Object> notes = new HashMap<>();

        /**
         * 除了SendProcessor、PathVariable、RequestHeader以外，其他的参数map => 参数名:参数对象信息
         */
        private Map<String, CatMethodParamInfo> params = new HashMap<>();

        /**
         * 出现在url上的参数{@link PathVariable} map => 参数名:参数对象信息
         */
        private Map<String, CatMethodParamInfo> pathParamIndexMap = new HashMap<>();

        /**
         * 出现在url上的参数{@link RequestHeader} map => 参数名:参数对象信息
         */
        private Map<String, CatMethodParamInfo> headerParamIndexMap = new HashMap<>();



        public CatMethodInfo build() {
            AnnotationAttributes attrs = getAttributes(method);
            postProcess(attrs);
            return new CatMethodInfo(this);
        }


        private AnnotationAttributes getAttributes(Method method) {
            StandardMethodMetadata metadata = new StandardMethodMetadata(method);
            Map<String, Object> methodAttributes = metadata.getAnnotationAttributes(CatMethod.class.getName());
            Catface catface = clientInfo.getCatface();
            if ( catface != null ) {//精简模式
                if ( methodAttributes == null ) {
                    methodAttributes = new HashMap<>();
                    methodAttributes.put("notes", new CatNote[0]);
                    methodAttributes.put("socket", clientInfo.getSocket());
                    methodAttributes.put("connect", clientInfo.getConnect());
                    methodAttributes.put("logsMod", clientInfo.getLogsMod());
                }
                String path = CatToosUtil.getDefaultRequestUrl(catface, serviceName, method);
                methodAttributes.put("value", path);
                methodAttributes.put("method", RequestMethod.POST);
                isCatface = true;
            }
            AnnotationAttributes attrs = AnnotationAttributes.fromMap(methodAttributes);
            return attrs;
        }


        private void postProcess(AnnotationAttributes attrs) {

            // userSave
            this.methodName = method.getName();

            // http://www.bugcat.cc
            this.host = clientInfo.getHost();

            // /user/save
            this.path = "/" + envProp.getProperty(attrs.getString("value")).replaceAll("^/", "");

            // post | get
            this.requestType = attrs.getEnum("method");

            // 其他自定义参数、标记
            Map<String, Object> noteMap = new HashMap<>();
            CatNote[] notes = attrs.getAnnotationArray("notes", CatNote.class);
            if ( notes != null && notes.length > 0 ) {
                for ( CatNote note : notes ) {
                    String value = CatToosUtil.defaultIfBlank(note.value(), "");
                    String key = CatToosUtil.isBlank(note.key()) ? value : note.key();  //如果 key属性为空，默认赋值value
                    if ( value.startsWith("${") ) {
                        noteMap.put(key, envProp.getProperty(value));   //初步解析 value上的${}变量
                    } else {
                        noteMap.put(key, value);
                    }
                }
            }
            this.notes = noteMap;

            // 控制日志打印
            this.logsMod = CatLogsMod.Def == attrs.getEnum("logsMod") ? clientInfo.getLogsMod() : attrs.getEnum("logsMod");

            //链接超时
            int connect = attrs.getNumber("connect");
            this.connect = connect < 0 ? -1 : (connect == 0 ? clientInfo.getConnect() : connect);

            //链接超时
            int socket = attrs.getNumber("socket");
            this.socket = socket < 0 ? -1 : (socket == 0 ? clientInfo.getSocket() : socket);

            //方法返回对象
            this.returnInfo = new CatMethodReturnInfo(method.getReturnType(), method.getGenericReturnType());

            this.postString = isCatface;    // 如果是精简模式，默认是post+json

            //是否已经出现过主要入参对象
            boolean hasPrimary = false;
            boolean isCatface = clientInfo.getCatface() != null;

            Parameter[] parameters = method.getParameters();
            for ( int idx = 0; idx < parameters.length; idx++ ) {

                Parameter parameter = parameters[idx];
                Class<?> pclazz = parameter.getType();

                //在url上追加的参数，不绑定到参数列表中
                PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
                if ( pathVariable != null ) {
                    String pathParam = pathVariable.value();
                    pathParamIndexMap.put(pathParam, CatMethodParamInfo.builder().index(idx).parameterType(pclazz).build());
                    continue;
                }
                RequestHeader header = parameter.getAnnotation(RequestHeader.class);
                if ( header != null ) {
                    String pathParam = header.value();
                    headerParamIndexMap.put(pathParam, CatMethodParamInfo.builder().index(idx).parameterType(pclazz).build());
                    continue;
                }

                //这个参数是SendProcessor、或者其子类，不绑定到参数列表中
                if ( CatSendProcessor.class.isAssignableFrom(parameter.getType()) ) {
                    if ( handlerIndex != null ) {
                        throw new IllegalArgumentException("方法上只容许出现一个SendProcessor入参！" + method.toString());
                    }
                    handlerIndex = Integer.valueOf(idx);

                } else {

                    //获取参数名称 interface被编译之后，方法上的参数名会被擦除，只能使用注解标记别名
                    String pname = null;
                    if ( isCatface ) { // 如果是精简模式，所有的入参统一使用arg0、arg1、arg2..命名
                        pname = "arg" + idx;
                    } else {
                        pname = CatToosUtil.getAnnotationValue(parameter, RequestParam.class, ModelAttribute.class, RequestHeader.class, CatNote.class);
                        if ( CatToosUtil.isBlank(pname) ) {
                            pname = parameter.getName();
                        }
                    }

                    CatMethodParamInfo.Builder builder = CatMethodParamInfo.builder().index(idx).parameterType(pclazz);

                    if ( parameter.isAnnotationPresent(ModelAttribute.class) || parameter.isAnnotationPresent(RequestBody.class) ) {
                        if ( hasPrimary ) {
                            throw new IllegalArgumentException("方法上只容许出现一个被@RequestBody、@ModelAttribute注解的入参！" + method.toString());
                        } else {
                            hasPrimary = true;
                            builder.primary(true);
                        }
                        //如果post方式，并且有@RequestBody注解
                        if ( this.requestType == RequestMethod.POST && parameter.isAnnotationPresent(RequestBody.class) ) {
                            postString = true;
                        }
                    }

                    // 有效参数
                    CatMethodParamInfo paramInfo = builder.build();
                    params.put(pname, paramInfo);
                }
            }
        }
    }


}
