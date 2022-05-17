package cc.bugcat.catclient.config;

import cc.bugcat.catclient.annotation.CatMethod;
import cc.bugcat.catface.utils.CatToosUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 *
 * 重连策略
 *
 * 最终每个配置项的匹配逻辑： note || (tags && method && ( status || exception ))
 *
 * @author bugcat
 * */
public class CatHttpRetryConfigurer implements InitializingBean {

    /**
     * 是否开启重连
     * */
    @Value("${retry.enable:false}")
    private boolean enable;


    /**
     * 重连次数
     *
     * 注意，重连次数不包含第一次调用！
     * retries=2，实际上最多会调用3次
     * */
    @Value("${retry.retrie:2}")
    private int retries;


    /**
     * 重连的状态码：多个用逗号隔开；
     * "500,501,401" or "400-410,500-519,419" or "*" or "any"
     * */
    @Value("${retry.status:500-520}")
    private String status;


    /**
     * 需要重连的请求方式：多个用逗号隔开；
     * "post,get" or "*" or "any"
     * */
    @Value("${retry.method:any}")
    private String method;


    /**
     * 需要重连的异常、或其子类；多个用逗号隔开；
     * "java.io.IOException" or "*" or "any"
     * */
    @Value("${retry.exception:}")
    private String exception;


    /**
     * 需要重连的api分组，在@CatClient中配置；多个用逗号隔开；
     * "some word" or "*" or "any"
     * */
    @Value("${retry.tags:}")
    private String tags;

    /**
     * 其他特殊标记；多个用逗号隔开；
     * 在@CatMethod中配置；
     * 会匹配方法上@CatNote注解，当retry.note设置的值，在@CatNote value中存在时，触发重连。
     *
     * "some word" 匹配 @CatNote("some word")
     * 如果@CatNote采用'#{arg.name}'形式，可以实现运行时，根据入参决定是否需要重连
     * */
    @Value("${retry.note:}")
    private String note;


    /**
     * 其他特殊标记匹配；在配置文件中，使用单引号包裹的json字符串；
     * 会匹配方法上{@link CatMethod#notes()}：当retry.note-match设置的键值对，在notes的键值对中完全匹配时，触发重连。
     *
     * '{"name":"bugcat","age":"17"}' 匹配 @CatNote(key="name", value="bugcat") + @CatNote(key="age", value="17")
     * 如果@CatNote采用'#{arg.name}'形式，可以实现运行时，根据入参决定是否需要重连
     * */
    @Value("${retry.note-match:{}}")
    private String noteMatch;



    //需要重连的状态码
    private List<StatusCode> statusCode = new ArrayList<>();
    //需要重连的异常类
    private Set<Class> exceptionCode = new HashSet<>();
    //需要重连的api分组
    private Set<String> tagsCode = new HashSet<>();
    //需要重连的标签
    private Set<String> noteCode = new HashSet<>();
    private Map<String, Object> noteMatchCode = new HashMap<>();


    @Override
    public void afterPropertiesSet() throws Exception {

        if ( retries <= 0 ) { //设置的重连次数小于0
            enable = false;
        }

        if ( enable ) {

            if ( CatToosUtil.isNotBlank(status) ) {
                status = "," + status + ",";
                if ( status.contains(",none,") ) {
                    statusCode.clear();
                } else  if( status.contains(",*,") || status.contains(",any,") ){
                    statusCode.add(new StatusCode(0, 999999));
                } else {
                    String[] codes = status.split(",");
                    for ( String code : codes ) {
                        if( CatToosUtil.isNotBlank(code) ){
                            StatusCode sc = null;
                            String[] cs = code.split("-");
                            if ( cs.length > 1 ) {
                                sc = new StatusCode(Integer.parseInt(cs[0]), Integer.parseInt(cs[1]));
                            } else {
                                sc = new StatusCode(Integer.parseInt(code));
                            }
                            statusCode.add(sc);
                        }
                    }
                }
            }

            if ( CatToosUtil.isNotBlank(method) ) {
                method = "," + method.trim().toUpperCase() + ",";
                if( method.contains(",ANY,") ){
                    method = "*";
                }
            }

            if ( CatToosUtil.isNotBlank(exception) ) {
                exception = "," + exception + ",";
                if( exception.contains(",none,") ){
                    exception = "";
                } else if( exception.contains(",any,") || exception.contains(",*,")){
                    exception = "*";
                } else {
                    for(String ex : exception.split(",")){
                        if( CatToosUtil.isNotBlank(ex) ){
                            Class clazz = Class.forName(ex.trim());
                            exceptionCode.add(clazz);
                        }
                    }
                }
            }

            if ( CatToosUtil.isNotBlank(tags) ){
                tags = "," + tags + ",";
                if( exception.contains(",none,") ){
                    tags = "";
                } else if( exception.contains(",any,") || exception.contains(",*,")){
                    tags = "*";
                } else {
                    for(String tag : tags.split(",")){
                        if( CatToosUtil.isNotBlank(tag) ){
                            tagsCode.add(tag.trim());
                        }
                    }
                }
            }

            if ( CatToosUtil.isNotBlank(note) ){
                for(String nt : note.split(",")){
                    if( CatToosUtil.isNotBlank(nt) ){
                        noteCode.add(nt.trim());
                    }
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
    public boolean containsTags(Map<String, String> tagMap){
        if( "*".equals(this.tags) ) {
            return true;
        }
        for ( String tag : tagMap.keySet() ) {
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
            Object value = entry.getValue();
            Object noteValue = noteMap.get(entry.getKey());
            if ( value != null && noteValue != null && value.equals(noteValue) ) {
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
