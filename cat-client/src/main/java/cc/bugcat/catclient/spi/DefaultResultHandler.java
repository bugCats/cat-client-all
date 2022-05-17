package cc.bugcat.catclient.spi;

import cc.bugcat.catclient.beanInfos.CatClientInfo;
import cc.bugcat.catclient.beanInfos.CatMethodInfo;
import cc.bugcat.catclient.beanInfos.CatMethodReturnInfo;
import cc.bugcat.catclient.handler.CatHttpException;
import cc.bugcat.catclient.handler.CatSendContextHolder;
import cc.bugcat.catface.spi.AbstractResponesWrapper;

import java.lang.reflect.Constructor;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 默认的结果处理类
 *
 * @author bugcat
 * */
public class DefaultResultHandler implements CatResultProcessor {


    /**
     * 是否可以重试
     * */
    @Override
    public boolean canRetry(CatHttpException exception, CatSendContextHolder context) {
        CatSendProcessor sendHandler = context.getSendHandler();
        return sendHandler.canRetry(context, exception);
    }


    /**
     * 打发生http异常时，默认继续抛出
     * */
    @Override
    public boolean onHttpError(CatSendContextHolder context) throws Throwable {
        throw context.getException();
    }


    /**
     * 字符串转方法返回对象
     * */
    @Override
    public Object resultToBean(String result, CatSendContextHolder context) {
        if( result == null ){
            return null;
        }
        CatMethodInfo methodInfo = context.getMethodInfo();
        CatClientInfo catClientInfo = context.getClientInfo();
        CatJsonResolver resolver = context.getFactoryAdapter().getJsonResolver();
        CatMethodReturnInfo returnInfo = methodInfo.getReturnInfo();
        AbstractResponesWrapper wrapperHandler = catClientInfo.getWrapperHandler();

        Class returnClass = returnInfo.getClazz();
        if( String.class.equals(returnClass) ){
            return result;
        }

        // 没有设置包装器类
        if ( wrapperHandler == null ) {

            //日期、基本数据类型、及包装类
            if(returnInfo.isSimple()){
                return toSimpleBean(result, returnInfo);
            } else if ( java.sql.Date.class.isAssignableFrom(returnInfo.getClazz()) ){
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mi:ss.SSS");   //非线程安全
                ParsePosition pos = new ParsePosition(0);
                Date strtodate = formatter.parse(result, pos);
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
    public Object doFinally(Object resp, CatSendContextHolder context) throws Exception {
        CatClientInfo clientInfo = context.getClientInfo();
        CatMethodInfo methodInfo = context.getMethodInfo();

        AbstractResponesWrapper wrapperHandler = clientInfo.getWrapperHandler();
        if ( wrapperHandler == null || resp == null ) {
            return resp;
        }
        Class wrapperClass = wrapperHandler.getWrapperClass();
        Class returnClass = methodInfo.getReturnInfo().getClazz();

        //方法的响应，与包装器类型相同，直接返回对象
        if( wrapperClass.equals(returnClass) ){
            return resp;
        } else if( wrapperClass.isAssignableFrom(resp.getClass()) ){
            // 方法的响应与包装器类型不同相同，并且响应类型是包装器类：拆包裹、校验
            wrapperHandler.checkValid(resp);
            return wrapperHandler.getValue(resp);
        }else {
            return resp;
        }
    }


    /**
     * 将 String 强制转换 clazz对应的简单对象
     */
    public static Object toSimpleBean(String text, CatMethodReturnInfo returnInfo) {
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
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * 得到Number类型的具体子类
     */
    public static Object getNumberType(String text, String className, Class clazz) {
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
            } catch ( Exception e ) { }
        }
        return text;
    }

}
