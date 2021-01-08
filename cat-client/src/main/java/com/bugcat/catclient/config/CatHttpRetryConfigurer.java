package com.bugcat.catclient.config;

import com.bugcat.catface.utils.CatToosUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;


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
     */
    @Value("${retry.retrie:2}")
    private int retries;


    /**
     * 重试的状态码：
     * "500,501,401" or "400-499,500-599"
     */
    @Value("${retry.status:500-520}")
    private String status;


    /**
     * 需要重试的请求方式：
     * "post,get" or "all"
     */
    @Value("${retry.method:all}")
    private String method;


    /**
     * 需要重试的异常
     * "java.io.IOException" or "any"
     */
    @Value("${retry.exception:}")
    private String exception;


    /**
     * 其他特殊标记
     * "retry,try"
     */
    @Value("${retry.note:}")
    private String note;
    
    
    
    private Set<String> statusCode = new HashSet<>();
    private Set<String> noteCode = new HashSet<>();

    @PostConstruct
    public void init() {

        if ( retries <= 0 ) {
            enable = false;
        }

        if ( enable ) {

            if ( CatToosUtil.isNotBlank(status) ) {
                String[] codes = status.split(",");
                for ( String code : codes ) {
                    if ( code.contains("-") ) {
                        String[] tmp = code.split("-");
                        for ( int i = Integer.valueOf(tmp[0]), e = Integer.valueOf(tmp[1]); i <= e; i++ ) {
                            statusCode.add(String.valueOf(i));
                        }
                    } else {
                        statusCode.add(code);
                    }
                }
            }

            if ( CatToosUtil.isNotBlank(method) ) {
                if( "all".equalsIgnoreCase(method) ){
                    method = "*";
                } else {
                    method = "," + method.toUpperCase() + ",";
                }
            }

            if ( CatToosUtil.isNotBlank(exception) ) {
                if( "any".equalsIgnoreCase(exception) ){
                    exception = "*";
                } else {
                    exception = "," + exception.toUpperCase() + ",";
                }
            }
            
            if ( CatToosUtil.isNotBlank(note) ){
                Arrays.stream(note.split(",")).forEach(nt -> noteCode.add(nt));
            }
        }
    }

    
    public boolean containsStatus(Integer status){
        return status != null && this.statusCode.contains(String.valueOf(status));
    }
    public boolean containsMethod(String method){
        return "*".equals(this.method) || this.method.contains("," + method.toUpperCase() + ",");
    }
    public boolean containsException(String exception){
        return "*".equals(this.exception) || this.exception.contains("," + exception.toUpperCase() + ",");
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
        return false;
    }
    
    

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
