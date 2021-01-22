package com.bugcat.catclient.spi;

import com.alibaba.fastjson.JSONObject;
import com.bugcat.catclient.beanInfos.CatClientInfo;
import com.bugcat.catclient.beanInfos.CatMethodInfo;
import com.bugcat.catclient.beanInfos.CatMethodReturnInfo;
import com.bugcat.catclient.config.CatHttpRetryConfigurer;
import com.bugcat.catclient.config.CatJsonObjectResolverConfigurer;
import com.bugcat.catclient.handler.CatHttpException;
import com.bugcat.catclient.handler.ResultProcessor;
import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.catclient.utils.CatClientUtil;
import com.bugcat.catface.spi.ResponesWrapper;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Constructor;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.bugcat.catclient.config.CatJsonObjectResolverConfigurer.JsonObjectResolver;

/**
 * 默认的结果处理类
 * @author bugcat
 * */
public class DefaultResultHandler extends ResultProcessor {


    @Override
    public boolean canRetry(CatHttpRetryConfigurer retryConfigurer, CatHttpException ex, CatClientInfo clientInfo, SendProcessor sendHandler) {
        
        if( retryConfigurer == null ){
            return false;
        }
        
        JSONObject notes = sendHandler.getNotes();
        int retry = (int) notes.getOrDefault(CatHttpRetryConfigurer.RETRY_COUNT, retryConfigurer.getRetries());
        if ( retryConfigurer.isEnable() && retry > 0) {
            boolean note = retryConfigurer.containsNote(notes);
            boolean tags = retryConfigurer.containsTags(clientInfo.getTags());
            boolean method = retryConfigurer.containsMethod(sendHandler.getRequestType().name());
            boolean status = retryConfigurer.containsStatus(ex.getStatus());
            boolean exception = retryConfigurer.containsException(ex.getIntrospectedClass());
            if( note || (tags && method && ( status || exception )) ){
                notes.put(CatHttpRetryConfigurer.RETRY_COUNT, retry - 1); //重连次数减一
                return true;
            }
        }
        
        return false;
    }


    @Override
    public Object onHttpError(Exception exception, SendProcessor sendHandler, CatClientInfo catClientInfo, CatMethodInfo methodInfo) throws Exception {
        throw exception;
    }

    
    
    @Override
    public Object resultToBean(String resp, SendProcessor sendHandler, CatClientInfo catClientInfo, CatMethodInfo methodInfo) {

        if( resp == null ){
            return null;
        }

        CatJsonObjectResolverConfigurer resolverConfigurer = CatClientUtil.getBean(CatJsonObjectResolverConfigurer.class);
        JsonObjectResolver resolver = resolverConfigurer.getResolver();
        
        CatMethodReturnInfo returnInfo = methodInfo.getReturnInfo();

        ResponesWrapper wrapper = ResponesWrapper.getResponesWrapper(catClientInfo.getWrapper());

        // 没有设置包装器类
        if ( wrapper == null ) {
            
            //日期、基本数据类型、及包装类
            if(returnInfo.isSimple()){
                
                return toSimpleBean(resp, returnInfo);
                
            } else if ( java.sql.Date.class.isAssignableFrom(returnInfo.getClazz()) ){
                
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mi:ss.SSS");   //非线程安全
                ParsePosition pos = new ParsePosition(0);
                Date strtodate = formatter.parse(resp, pos);
                return strtodate;
                
            } else {//复杂对象
                return resolver.toJavaBean(resp, returnInfo.getType());
            }
        } else {

            //设置了包装器类
            Class returnClass = methodInfo.getReturnInfo().getClazz();
            Class<ResponseEntity> wrapperClass = wrapper.getWrapperClass();
            
            //方法的响应，与包装器类型相同
            if( returnClass.equals(wrapperClass) ) {
                return resolver.toJavaBean(resp, returnInfo.getType());
            } else {
                return resolver.toJavaBean(resp, wrapper, returnInfo.getType());
            }
        }
    }


    @Override
    public Object doFinally(Object resp, SendProcessor sendHandler, CatClientInfo catClientInfo, CatMethodInfo methodInfo) throws Exception {
        ResponesWrapper wrapper = ResponesWrapper.getResponesWrapper(catClientInfo.getWrapper());
        if ( wrapper == null ) {
            return resp;
        }
        Class wrapperClass = wrapper.getWrapperClass();
        Class returnClass = methodInfo.getReturnInfo().getClazz();
        
        //方法的响应，与包装器类型相同，直接返回对象
        if( wrapperClass.equals(returnClass) ){
            return resp;
        } else {
            // 方法的响应，与包装器类型不相同，拆包裹、校验
            wrapper.checkValid(resp);
            return wrapper.getValue(resp);
        }
    }

    
    
    
    /**
     * 将 String 强制转换 clazz对应的简单对象
     */
    protected static Object toSimpleBean(String str, CatMethodReturnInfo returnInfo) {
        
        String returnName = returnInfo.getName();

        if( "STRING".equals(returnName) ) {
            
            return str;

        } else if ("VOID".equals(returnName)) { //返回值类型为 void Void
            
            return null;
            
        } else if (Number.class.isAssignableFrom(returnInfo.getClazz())) {
            
            return getNumberType(str, returnName, returnInfo.getClazz());
            
        } else if ("BOOLEAN".equals(returnName)) {
            
            return Boolean.valueOf(str.toString());

        } else {
            try {
                return returnInfo.getClazz().cast(str);
            } catch (Exception e) {
                return null;
            }
        }
    }

    
    
    /**
     * 得到Number类型的具体子类
     */
    protected static Object getNumberType(String str, String className, Class clazz) {
        if( "".equals(str) ){
            return null;
        }
        if ("LONG".equals(className)) {
            return Long.valueOf(str);
        } else if ("INTEGER".equals(className) || "INT".equals(className)) {
            return Integer.valueOf(str);
        } else if ("DOUBLE".equals(className)) {
            return Double.valueOf(str);
        } else if ("FLOAT".equals(className)) {
            return Float.valueOf(str);
        } else if ("SHORT".equals(className)) {
            return Short.valueOf(str);
        } else if ("BYTE".equals(className)) {
            return Byte.valueOf(str);
        } else if ("CHARACTER".equals(className) || "CHAR".equals(className)) {
            return Character.codePointAt(str, 0);
        } else {
            try {
                Constructor constructor = clazz.getConstructor(String.class);
                return constructor.newInstance(str);
            } catch ( Exception e ) { }
            return str;
        }
    }


}
