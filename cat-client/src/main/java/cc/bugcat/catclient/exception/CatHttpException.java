package cc.bugcat.catclient.exception;

import cc.bugcat.catface.utils.CatToosUtil;

/**
 * http异常
 *
 * @author bugcat
 * */
public class CatHttpException extends Exception {

    private final Integer statusCode;       //异常代码
    private final String statusText;
    private final Throwable exception;      //原始异常


    public CatHttpException(int statusCode, String statusText, Exception exception) {
        super(CatToosUtil.getCause(exception));
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.exception = exception;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
    public String getStatusText() {
        return statusText;
    }

    public Throwable getIntrospectedException(){
        return super.getCause() != null ? getCause() : exception;
    }

    public Class<? extends Throwable> getIntrospectedClass() {
        return this.getIntrospectedException().getClass();
    }

}
