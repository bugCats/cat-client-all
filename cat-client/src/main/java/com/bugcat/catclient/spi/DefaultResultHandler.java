package com.bugcat.catclient.spi;

import com.alibaba.fastjson.JSONObject;
import com.bugcat.catclient.beanInfos.CatClientInfo;
import com.bugcat.catclient.beanInfos.CatMethodInfo;
import com.bugcat.catclient.beanInfos.CatMethodsReturnInfo;
import com.bugcat.catclient.handler.ResultProcessor;
import com.bugcat.catclient.handler.SendProcessor;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Constructor;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 默认的结果处理类
 * @author bugcat
 * */
public class DefaultResultHandler extends ResultProcessor {


    /**
     * 如果发生了40x、50x等异常，默认的异常处理方式
     * 如果结果返回String，会继续执行 resultToBean、doFinally 方法；否则直接执行atLast
     * @return String => 继续执行 resultToBean、doFinally 方法；
     *         Bean   => 继续执行 doFinally 方法；
     *
     * */
    @Override
    public Object onHttpError(Exception exception, SendProcessor sendHandler, CatClientInfo catClientInfo, CatMethodInfo methodInfo) throws Exception {
        throw exception;
    }

    
    
    /**
     * 将返回字符串，转换成对象
     * 
     * @param methodInfo    方法返回值数据类型信息，用来将http响应，转换成对象 -> new TypeReference<T>(){}
     * @return
     */
    @Override
    public Object resultToBean(String resp, SendProcessor sendHandler, CatClientInfo catClientInfo, CatMethodInfo methodInfo) {

        if( resp == null ){
            return null;
        }
        
        CatMethodsReturnInfo returnInfo = methodInfo.getReturnInfo();

        ResponesWrapper wrapper = ResponesWrapper.getResponesWrapper(catClientInfo.getWrapper());
        
        if ( wrapper == null ) {// 没有设置包裹类
            
            //日期、基本数据类型、及包装类
            if(returnInfo.isSimple()){
                
                return toSimpleBean(resp, returnInfo);
                
            } else if ( java.sql.Date.class.isAssignableFrom(returnInfo.getClazz()) ){
                
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mi:ss.SSS");
                ParsePosition pos = new ParsePosition(0);
                Date strtodate = formatter.parse(resp, pos);
                return strtodate;
                
            } else {//复杂对象
                
                return JSONObject.parseObject(resp, returnInfo.getType());
            }
        } else {

            //设置了包裹类
            Class returnClass = methodInfo.getReturnInfo().getClazz();
            Class<ResponseEntity> wrapperClass = wrapper.getWrapperClass();
            if( returnClass.equals(wrapperClass) ) { //方法的响应，与包裹类型相同
                return JSONObject.parseObject(resp, returnInfo.getType());
            } else {
                return JSONObject.parseObject(resp, wrapper.getWrapperType(returnInfo.getType()));
            }
            
        }
    }
    
   
 


    /**
     * 在toBean之后执行
     * 获取到对象之后，再做处理
     * @see ResponesWrapper
     * 
     * @param resp 经过toBean方法转换后的参数
     */
    @Override
    public Object doFinally(Object resp, SendProcessor sendHandler, CatClientInfo catClientInfo, CatMethodInfo methodInfo) throws Exception {
        ResponesWrapper wrapper = ResponesWrapper.getResponesWrapper(catClientInfo.getWrapper());
        if ( wrapper == null ) {
            return resp;
        }
        Class wrapperClass = wrapper.getWrapperClass();
        Class returnClass = methodInfo.getReturnInfo().getClazz();
        if( wrapperClass.equals(returnClass) ){    //方法的响应，与包裹类型相同，直接返回对象
            return resp;
        } else {// 方法的响应，与包裹类型不相同，拆包裹
            wrapper.checkValid(resp);    //校验
            return wrapper.getValue(resp);
        }
    }

    

    

    
    /**
     * 将 String 强制转换 clazz对应的简单对象
     */
    protected static Object toSimpleBean(String str, CatMethodsReturnInfo returnInfo) {
        
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
