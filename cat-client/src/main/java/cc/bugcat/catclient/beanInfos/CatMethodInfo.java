package cc.bugcat.catclient.beanInfos;


import cc.bugcat.catclient.annotation.CatMethod;
import cc.bugcat.catclient.handler.CatLogsMod;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.catface.annotation.CatNote;
import cc.bugcat.catface.annotation.CatNotes;
import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.catface.handler.EnvironmentAdapter;
import cc.bugcat.catface.utils.CatToosUtil;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * interface上的{@link CatMethod}注解描述信息
 *
 *
 * @author bugcat
 * */
public class CatMethodInfo {

    
    /**
     * 方法
     * */
    private final Method method;

    /**
     * 域名：http://${host}/ctx
     * 此时${host}已经被变量填充
     * */
    private final String host;

    /**
     * 调用的url
     * 从@CatMethod注解中获取的原始数据，可以包含{PathVariable}参数
     * */
    private final String path;

    /**
     * 方法上自定义参数、标记
     * */
    private final Map<String, String> noteMap;

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
     * 日志记录方案
     * */
    private final CatLogsMod logsMod;

    /**
     * http请求读写超时
     * */
    private final int connect;
    private final int socket;

    /**
     * 除了SendProcessor、PathVariable、RequestHeader以外，其他的参数map => 参数名:参数对象信息
     * */
    private final Map<String, CatMethodParamInfo> paramInfoMap;

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
     * SendProcessor 在参数列表中出现的索引。
     * 为null，表示需要通过{@link CatClientFactory#newSendHandler()}自动生成
     * */
    private final Integer handlerIndex;

    /**
     * 方法上包含的注解
     * */
    private final ConcurrentMap<Class, Annotation> annMap = new ConcurrentHashMap<>();
    
    
    private CatMethodInfo(CatMethodInfoBuilder builder) {
        this.method = builder.method;
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

        this.noteMap = Collections.unmodifiableMap(builder.noteMap);
        this.paramInfoMap = Collections.unmodifiableMap(builder.paramNameMap);
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
        String realPath = this.path;
        if ( pathParamIndexMap.size() > 0 ) {//填充 url 上的参数
            for ( Map.Entry<String, CatMethodParamInfo> entry : pathParamIndexMap.entrySet() ) {
                realPath = realPath.replace("{" + entry.getKey() + "}", CatToosUtil.toStringIfBlank(args[entry.getValue().getIndex()], ""));
            }
            realPath = realPath.replaceAll("/+", "/");
        }
        param.setRealPath(realPath);


        // 处理header参数
        Map<String, String> headerMap = new HashMap<>();
        if ( headerParamIndexMap.size() > 0 ) {//填充 url 上的参数
            for ( Map.Entry<String, CatMethodParamInfo> entry : headerParamIndexMap.entrySet() ) {
                headerMap.put(entry.getKey(), String.valueOf(args[entry.getValue().getIndex()]));  // entry.getValue().getIndex()=该参数，在方法上出现的索引值
            }
        }
        param.setHeaderMap(headerMap);


        // 将入参数组args，转换成： 参数名->入参    此时argsMap中一定不包含SendProcessor
        Map<String, Object> argsMap = new HashMap<>();
        paramInfoMap.forEach((key, value) -> {
            argsMap.put(key, args[value.getIndex()]);  // value.getIndex() 等于该参数在方法上出现的索引值
        });
        param.setArgsMap(argsMap);

        Object value = null;
        if ( paramInfoMap.size() == 1 ) {//如果入参仅一个
            Map.Entry<String, Object> entry = argsMap.entrySet().iterator().next();
            CatMethodParamInfo paramInfo = paramInfoMap.get(entry.getKey());
            if ( paramInfo.isPrimary() || !paramInfo.isSimple() ) {
                //被@RequestBody、@ModelAttribute标记了、或者是复杂对象，直接返回对象
                value = entry.getValue();
            } else {
                //方法上参数列表都是简单数据类型，返回一个map
                value = argsMap;
            }
        } else {
            //入参是多个，转成键值对
            value = argsMap;
        }
        param.setValue(value);
        return param;
    }



    public CatLogsMod getLogsMod() {
        return logsMod;
    }
    public Integer getHandlerIndex() {
        return handlerIndex;
    }
    public String getMethodName() {
        return method.getName();
    }
    public String getHost() {
        return host;
    }
    public Map<String, String> getNoteMap() {
        return noteMap;
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
    public <A> A findAnnotation(Class<A> annotationType){
        Annotation ann = annMap.get(annotationType);
        if( ann == null ){
            ann = AnnotationUtils.findAnnotation(method, (Class) annotationType);
            annMap.putIfAbsent(annotationType, ann);
        }
        return (A) ann;
    }



    /**********************************************************************************************************************/



    public static CatMethodInfoBuilder builder(Method method, CatClientInfo clientInfo, EnvironmentAdapter envProp) {
        CatMethodInfoBuilder builder = new CatMethodInfoBuilder(method, clientInfo, envProp);
        return builder;
    }


    public static class CatMethodInfoBuilder {

        private final Method method;
        private final CatClientInfo clientInfo;
        private final EnvironmentAdapter envProp;

        private CatMethodInfoBuilder(Method method, CatClientInfo clientInfo, EnvironmentAdapter envProp) {
            this.method = method;
            this.clientInfo = clientInfo;
            this.envProp = envProp;
        }

        /**
         * 域名  eq：http://xxxx，此时${host}已经被变量填充
         * */
        private String host;

        /**
         * 调用的url，从@CatMethod注解中获取的原始数据，可以包含{PathVariable}
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
         * 日志记录方案
         * */
        private CatLogsMod logsMod;

        /**
         * 方法返回参数对象
         * */
        private CatMethodReturnInfo returnInfo;

        /**
         * SendProcessor 在参数列表中出现的索引
         * 为null，表示需要通过{@link CatClientFactory#newSendHandler()}自动生成
         * */
        private Integer handlerIndex;

        /**
         * 方法上自定义参数、标记
         * */
        private Map<String, String> noteMap = new LinkedHashMap<>();

        /**
         * 除了SendProcessor、PathVariable、RequestHeader以外，其他的参数map => 参数名:参数对象信息
         * */
        private Map<String, CatMethodParamInfo> paramNameMap = new LinkedHashMap<>();

        /**
         * 出现在url上的参数{@link PathVariable} map => 参数名:参数对象信息
         * */
        private Map<String, CatMethodParamInfo> pathParamIndexMap = new LinkedHashMap<>();

        /**
         * 出现在url上的参数{@link RequestHeader} map => 参数名:参数对象信息
         * */
        private Map<String, CatMethodParamInfo> headerParamIndexMap = new LinkedHashMap<>();


        public CatMethodInfo build() {
            AnnotationAttributes attributes = getAttributes(method);
            postProcess(attributes);
            return new CatMethodInfo(this);
        }


        private AnnotationAttributes getAttributes(Method method) {
            StandardMethodMetadata metadata = new StandardMethodMetadata(method);
            Map<String, Object> methodAttrs = metadata.getAnnotationAttributes(CatMethod.class.getName());

            Catface catface = clientInfo.getCatface();
            this.isCatface = catface != null;
            
            if ( isCatface ) {//是精简模式
                if ( methodAttrs == null ) {
                    //其他使用interface上的注解
                    methodAttrs = new HashMap<>();
                    methodAttrs.put("notes", new CatNote[0]);
                    methodAttrs.put("socket", clientInfo.getSocket());
                    methodAttrs.put("connect", clientInfo.getConnect());
                    methodAttrs.put("logsMod", clientInfo.getLogsMod());
                }
                
                CatToosUtil.parseDefaultRequest(catface, clientInfo.getClientClassName(), method, methodAttrs);
            }
            
            /**
             * 当存在CatNotes时，会直接覆盖{@link CatMethod#notes()}
             * */
            CatNote[] catNotes = CatToosUtil.getCatNotes(method, CatNotes.Scope.Cilent);
            if( catNotes != null ){
                methodAttrs.put("notes", catNotes);
            }
            
            AnnotationAttributes attrs = AnnotationAttributes.fromMap(methodAttrs);
            return attrs;
        }


        private void postProcess(AnnotationAttributes attrs) {

            // http://www.bugcat.cc
            this.host = clientInfo.getHost();

            // /user/save
            String url = attrs.getString("value");
            this.path = clientInfo.getBasePath() + envProp.getProperty(url);

            // post | get
            this.requestType = attrs.getEnum("method");

            // 其他自定义参数、标记
            Map<String, String> noteMap = new LinkedHashMap<>();
            CatNote[] notes = attrs.getAnnotationArray("notes", CatNote.class);
            for ( CatNote note : notes ) {
                String value = CatToosUtil.defaultIfBlank(note.value(), "");

                //如果 key属性为空，默认赋值value
                String key = CatToosUtil.isBlank(note.key()) ? value : note.key();
                noteMap.put(key, value);
            }

            this.noteMap = noteMap;

            // 控制日志打印
            this.logsMod = CatToosUtil.comparator(CatLogsMod.Def, Arrays.asList(attrs.getEnum("logsMod")), clientInfo.getLogsMod());

            //链接超时
            int connect = attrs.getNumber("connect");
            connect = connect < 0 ? -1 : connect;
            this.connect = CatToosUtil.comparator(0, Arrays.asList(connect), clientInfo.getConnect());

            //链接超时
            int socket = attrs.getNumber("socket");
            socket = socket < 0 ? -1 : socket;
            this.socket = CatToosUtil.comparator(0, Arrays.asList(socket), clientInfo.getSocket());

            //方法返回对象
            this.returnInfo = new CatMethodReturnInfo(method.getReturnType(), method.getGenericReturnType());

            //如果是精简模式，默认是post+json
            this.postString = isCatface;

            //是否已经出现过主要入参对象
            boolean hasPrimary = false;

            Parameter[] parameters = method.getParameters();
            for ( int idx = 0; idx < parameters.length; idx++ ) {

                Parameter parameter = parameters[idx];
                Class pclazz = parameter.getType();

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
                    continue;
                }


                //获取参数名称 interface被编译之后，方法上的参数名会被擦除，只能使用注解标记别名
                String pname = CatToosUtil.getAnnotationValue(parameter, RequestParam.class, ModelAttribute.class, CatNote.class);
                if ( CatToosUtil.isBlank(pname) ) { //如果没有为入参取别名
                    if ( isCatface ) { // catface模式，入参统一使用arg0、arg1、arg2、argX...命名
                        pname = "arg" + idx;
                    } else { //普通模式，使用编译后的入参名
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
                paramNameMap.put(pname, paramInfo);
            }
        }
    }


}
