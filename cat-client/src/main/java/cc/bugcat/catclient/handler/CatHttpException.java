package cc.bugcat.catclient.handler;

/**
 * http异常
 *
 * @author bugcat
 * */
public class CatHttpException extends Exception {

    private final Integer statusCode;         //异常代码
    private final String statusText;
    private final Exception exception;    //原始异常


    public CatHttpException(int statusCode, String statusText, Exception exception) {
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

    public Exception getIntrospectedException(){
        return exception;
    }

    public Class<? extends Exception> getIntrospectedClass() {
        return exception.getClass();
    }


}
