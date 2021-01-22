package com.bugcat.catclient.config;

import com.alibaba.fastjson.JSONObject;
import com.bugcat.catface.utils.CatToosUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;


/**
 *
 * 重连策略
 *
 * 最终每个配置项的匹配逻辑： note || (tags && method && ( status || exception ))
 *
 * */
@Component
public class CatHttpRetryConfigurer {


    public static final String RETRY_COUNT = "bugcat$HttpRetryCount";


    /**
     * 是否开启重连
     */
    @Value("${retry.enable:false}")
    private boolean enable;


    /**
     * 重连次数
     *
     * 注意，重连次数不包含第一次调用！
     * retries=2，实际上最多会调用3次
     */
    @Value("${retry.retrie:2}")
    private int retries;


    /**
     * 重连的状态码：多个用逗号隔开；
     * "500,501,401" or "400-410,500-519"
     */
    @Value("${retry.status:500-520}")
    private String status;


    /**
     * 需要重连的请求方式：多个用逗号隔开；
     * "post,get" or "any" or "*"
     */
    @Value("${retry.method:any}")
    private String method;


    /**
     * 需要重连的异常、或其子类；多个用逗号隔开；
     * "java.io.IOException" or "any" or "*"
     */
    @Value("${retry.exception:java.io.IOException}")
    private String exception;


    /**
     * 需要重连的分组；多个用逗号隔开；
     * */
    @Value("${retry.tags:any}")
    private String tags;

    /**
     * 其他特殊标记；多个用逗号隔开；
     * 会匹配方法上@CatNote注解，当retry.note设置的值，在@CatNote value中存在时，触发重连
     *
     * "payOrder,userSave"
     */
    @Value("${retry.note:}")
    private String note;


    /**
     * 其他特殊标记匹配；在配置文件中，使用单引号包裹的json字符串
     * 会匹配方法上@CatNote注解，当retry.note-match设置的键值对，在@CatNote key-value中完全匹配时，触发重连
     * '{name:"bugcat",age:"17"}'
     */
    @Value("${retry.note-match:{}}")
    private String noteMatch;



    private List<StatusCode> statusCode = new ArrayList<>();
    private Set<Class> exceptionCode = new HashSet<>();
    private Set<String> tagsCode = new HashSet<>();
    private Set<String> noteCode = new HashSet<>();
    private Map<String, Object> noteMatchCode = new HashMap<>();


    @PostConstruct
    public void init() throws ClassNotFoundException {

        if ( retries <= 0 ) {
            enable = false;
        }

        if ( enable ) {

            if ( CatToosUtil.isNotBlank(status) ) {
                String[] codes = status.split(",");
                for ( String code : codes ) {
                    StatusCode sc = null;
                    if ( code.contains("-") ) {
                        String[] cs = code.split("-");
                        sc = new StatusCode(Integer.parseInt(cs[0]), Integer.parseInt(cs[1]));
                    } else {
                        sc = new StatusCode(Integer.parseInt(code));
                    }
                    statusCode.add(sc);
                }
            }

            if ( CatToosUtil.isNotBlank(method) ) {
                if( "any".equalsIgnoreCase(method) ){
                    method = "*";
                } else {
                    method = "," + method.trim().toUpperCase() + ",";
                }
            }

            if ( CatToosUtil.isNotBlank(exception) ) {
                if( "any".equalsIgnoreCase(exception) ){
                    exception = "*";
                } else {
                    for(String ex : exception.split(",")){
                        Class clazz = Class.forName(ex.trim());
                        exceptionCode.add(clazz);
                    }
                }
            }

            if ( CatToosUtil.isNotBlank(tags) ){
                if( "any".equalsIgnoreCase(tags) ){
                    tags = "*";
                } else {
                    for(String tag : tags.split(",")){
                        tagsCode.add(tag.trim());
                    }
                }
            }

            if ( CatToosUtil.isNotBlank(note) ){
                for(String nt : note.split(",")){
                    noteCode.add(nt.trim());
                }
            }

            if ( CatToosUtil.isNotBlank(noteMatch) ){
                JSONObject match = JSONObject.parseObject(noteMatch);
                noteMatchCode.putAll(match);
            }
        }
    }


    public boolean containsStatus(Integer status){
        if( status == null ){
            return false;
        }
        for(StatusCode sc : statusCode ){
            if( sc.start <= status && status <= sc.end ){
                return true;
            }
        }
        return false;
    }
    public boolean containsMethod(String method){
        return "*".equals(this.method) || this.method.contains("," + method.toUpperCase() + ",");
    }
    public boolean containsException(Class<? extends Throwable> ex){
        if( "*".equals(this.exception) || exceptionCode.contains(ex) ){
            return true;
        }
        for( Class clazz : exceptionCode ){
            if( clazz.isAssignableFrom(ex) ){
                return true;
            }
        }
        return false;
    }
    public boolean containsTags(String[] tags){
        if( "*".equals(this.tags) ) {
            return true;
        }
        for ( String tag : tags ) {
            if( tagsCode.contains(tag) ){
                return true;
            }
        }
        return false;
    }
    public boolean containsNote(Map<String, Object> noteMap){
        if ( noteMap == null ) {
            return false;
        }
        for ( String note : noteCode ) {
            if ( noteMap.get(note) != null ) {
                return true;
            }
        }
        for ( Map.Entry<String, Object> entry : noteMatchCode.entrySet() ) {
            Object value = noteMap.get(entry.getKey());
            if ( value != null && value.toString().equals(entry.getValue())) {
                return true;
            }
        }
        return false;
    }


    public boolean isEnable() {
        return enable;
    }

    public int getRetries() {
        return retries;
    }



    private static class StatusCode {
        private int start;
        private int end;
        public StatusCode(int code) {
            this(code, code);
        }
        public StatusCode(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
