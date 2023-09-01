package cc.bugcat.catclient.spi;

import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.beanInfos.CatMethodInfo;
import cc.bugcat.catclient.beanInfos.CatMethodReturnInfo;
import cc.bugcat.catclient.exception.PayloadResolverException;
import cc.bugcat.catclient.handler.CatClientContextHolder;
import cc.bugcat.catclient.exception.CatHttpException;
import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catface.utils.CatToosUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.lang.reflect.Constructor;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 默认的结果处理类
 *
 * @author bugcat
 * */
public class SimpleResultHandler implements CatResultProcessor {


    /**
     * 是否可以重试
     * */
    @Override
    public boolean canRetry(CatHttpException exception, CatClientContextHolder context) {
        CatSendProcessor sendHandler = context.getSendHandler();
        return sendHandler.canRetry(context, exception);
    }


    /**
     * 当发生http异常时，默认继续抛出
     * */
    @Override
    public boolean onHttpError(CatClientContextHolder context) throws Throwable {
        throw context.getException();
    }


    /**
     * 字符串转方法返回对象
     * */
    @Override
    public Object resultToBean(String result, CatClientContextHolder context) throws Exception {
        if( result == null ){
            return null;
        }
        CatMethodInfo methodInfo = context.getMethodInfo();
        CatMethodReturnInfo returnInfo = methodInfo.getReturnInfo();
        Class returnClass = returnInfo.getClazz();
        
        // 如果响应类型是Object，那么直接返回原始的字符串，不做任何处理
        if( Object.class.equals(returnClass) ){
            return result;
        }

        CatClientInfo clientInfo = context.getClientInfo();
        CatPayloadResolver resolver = context.getFactoryAdapter().getPayloadResolver();
        AbstractResponesWrapper wrapperHandler = clientInfo.getWrapperHandler();
        
        // 没有设置包装器类
        if ( wrapperHandler == null ) {
            if( void.class.equals(returnClass) ){
                return null;
            } else if(returnInfo.isSimple()){ //日期、基本数据类型、及包装类
                return toSimpleBean(result, returnInfo);
            } else if (Date.class.isAssignableFrom(returnInfo.getClazz())){
                String dateFormat = getDateFormat(context);
                SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
                Date strtodate = formatter.parse(result);
                return strtodate;
            } else {//复杂对象
                return resolver.toJavaBean(result, returnInfo.getType());
            }
        } else {

            //设置了包装器类
            Class wrapperClass = wrapperHandler.getWrapperClass();

            //方法的响应，与包装器类型相同
            if( returnClass.equals(wrapperClass) ) {
                return resolver.toJavaBean(result, returnInfo.getType());
            } else {
                return resolver.toJavaBean(result, wrapperHandler, returnInfo.getType());
            }
        }
    }


    @Override
    public Object onFinally(Object result, CatClientContextHolder context) throws Exception {
        CatClientInfo clientInfo = context.getClientInfo();
        CatMethodInfo methodInfo = context.getMethodInfo();

        AbstractResponesWrapper wrapperHandler = clientInfo.getWrapperHandler();
        if ( wrapperHandler == null || result == null ) {
            return result;
        }
        Class wrapperClass = wrapperHandler.getWrapperClass();
        Class returnClass = methodInfo.getReturnInfo().getClazz();

        //方法的响应，与包装器类型相同，直接返回对象
        if( wrapperClass.equals(returnClass) ){
            return result;
        } else if( wrapperClass.isAssignableFrom(result.getClass()) ){
            // 方法的响应与包装器类型不同相同，并且响应类型是包装器类：拆包裹、校验
            wrapperHandler.checkValid(result);
            return wrapperHandler.getValue(result);
        }else {
            return result;
        }
    }

    /**
     * 获取方法上关于Date的格式
     * */
    protected String getDateFormat(CatClientContextHolder context){
        String format = null;
        CatMethodInfo methodInfo = context.getMethodInfo();
        JsonFormat jsonFormat = methodInfo.findAnnotation(JsonFormat.class);
        if ( jsonFormat != null ) {
            format = jsonFormat.pattern();
        }
        if ( CatToosUtil.isBlank(format) ){
            JSONField jsonField = methodInfo.findAnnotation(JSONField.class);
            if( jsonField != null ){
                format = jsonField.format();
            }
        }
        if ( CatToosUtil.isBlank(format) ){
            format = "yyyy-mm-dd HH:mi:ss.SSS";
        }
        return format;
    }

    /**
     * 将 String 强制转换 clazz对应的简单对象
     * */
    public Object toSimpleBean(String text, CatMethodReturnInfo returnInfo) {
        String returnName = returnInfo.getName();
        if( "STRING".equals(returnName) ) {
            return text;
        } else if ("VOID".equals(returnName)) { //返回值类型为 void Void
            return null;
        } else if (Number.class.isAssignableFrom(returnInfo.getClazz())) {
            return getNumberType(text, returnName, returnInfo.getClazz());
        } else if ("BOOLEAN".equals(returnName)) {
            return Boolean.valueOf(text);
        } else {
            try {
                return returnInfo.getClazz().cast(text);
            } catch (Exception ex) {
                throw new PayloadResolverException("[" + text + "]转" + returnInfo.getClazz().getName() + "异常", ex);
            }
        }
    }

    /**
     * 得到Number类型的具体子类
     * */
    public Object getNumberType(String text, String className, Class clazz) {
        if( "".equals(text) ){
            return null;
        }
        if ("LONG".equals(className)) {
            return Long.valueOf(text);
        } else if ("INTEGER".equals(className) || "INT".equals(className)) {
            return Integer.valueOf(text);
        } else if ("DOUBLE".equals(className)) {
            return Double.valueOf(text);
        } else if ("FLOAT".equals(className)) {
            return Float.valueOf(text);
        } else if ("SHORT".equals(className)) {
            return Short.valueOf(text);
        } else if ("BYTE".equals(className)) {
            return Byte.valueOf(text);
        } else if ("CHARACTER".equals(className) || "CHAR".equals(className)) {
            return Character.codePointAt(text, 0);
        } else {
            try {
                Constructor constructor = clazz.getConstructor(String.class);
                return constructor.newInstance(text);
            } catch ( Exception ex ) {
                throw new PayloadResolverException("[" + text + "]转" + className + "异常", ex);
            }
        }
    }

}
