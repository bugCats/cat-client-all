package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.exception.CatHttpException;
import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.catface.utils.CatToosUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * http请求日志类，多例。
 * 每次调用http，都会重新生成一个新对象
 * */
public class CatClientLogger {


    /**
     * 如果有重连机制，那么调用一次方法，可能会执行多次http请求，会创建多个CatClientLogger对象，但是tracerId是相同的
     * @see CatSendProcessor#getTracerId()
     * */
    private String tracerId;

    /**
     * 日志记录方案
     */
    private CatLogsMod logsMod;
    
    private Class clientClass;
    private Logger logger;
    
    private long executeTime;       //调用http耗时

    private String errorMessge;             //错误信息，如果不为null，说明调用失败
    private CatHttpException exception;     //错误信息，如果不为null，说明调用失败
    
    private String apiName;     //方法名

    private String apiUrl;      //最终请求url

    private String request;     //输入报文

    private String response;    //响应报文


    private List<String> infos = new LinkedList<>();

    public CatClientLogger() {

    }

    /**
     * 默认日志输出格式
     * */
    public String toJson(){
        
        Map<String, Object> logInfo = new LinkedHashMap<>();
        logInfo.put("@tracerId", tracerId);
        logInfo.put("@name", clientClass.getSimpleName() + "." + apiName);
        logInfo.put("@url", apiUrl);
        logInfo.put("@in", "#{in}");
        logInfo.put("@out", "#{out}");
        logInfo.put("@info", infos.toString());
        logInfo.put("@time", executeTime + "ms");

        boolean printIn = CatLogsMod.All == logsMod || CatLogsMod.In == logsMod;
        boolean printOut = CatLogsMod.All == logsMod || CatLogsMod.Out == logsMod;

        if( this.isSuccess() ){
            logInfo.put("@succ", "1");
        } else {
            logInfo.put("@succ", "0");
            logInfo.put("@error", errorMessge != null ? errorMessge : String.valueOf(exception.getStatusCode()) + " - " + exception.getStatusText());

            printIn = printIn || (CatLogsMod.All2 == logsMod || CatLogsMod.In2 == logsMod);
            printOut = printOut || (CatLogsMod.All2 == logsMod || CatLogsMod.Out2 == logsMod);
        }

        String logs = JSONObject.toJSONString(logInfo)
                .replace("\"#{in}\"", printIn ? CatToosUtil.defaultIfBlank(request, "\"\"") : "\"\"")
                .replace("\"#{out}\"", printOut ? CatToosUtil.defaultIfBlank(response, "\"\"") : "\"\"");

        return logs;
    }


    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public boolean isSuccess(){
        return exception == null && errorMessge == null;
    }

    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public boolean isFail(){
        return !isSuccess();
    }

    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public CatClientLogger info(String info){
        infos.add(info);
        return this;
    }


    public String getTracerId() {
        return tracerId;
    }
    public void setTracerId(String tracerId) {
        this.tracerId = tracerId;
    }

    public CatLogsMod getLogsMod() {
        return logsMod;
    }
    public void setLogsMod(CatLogsMod logsMod) {
        this.logsMod = logsMod;
    }

    public long getExecuteTime() {
        return executeTime;
    }
    public void setExecuteTime(long executeTime) {
        this.executeTime = executeTime;
    }
    
    public String getErrorMessge() {
        return errorMessge;
    }
    public void setErrorMessge(String errorMessge) {
        this.errorMessge = errorMessge;
    }

    public CatHttpException getException() {
        return exception;
    }
    public void setException(CatHttpException exception) {
        this.exception = exception;
    }

    public Class getClientClass() {
        return clientClass;
    }
    public void setClientClass(Class clientClass) {
        this.clientClass = clientClass;
    }

    public Logger getLogger() {
        return logger;
    }
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public String getApiName() {
        return apiName;
    }
    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiUrl() {
        return apiUrl;
    }
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getRequest() {
        return request;
    }
    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }
    public void setResponse(String response) {
        this.response = response;
    }

    public List<String> getInfos() {
        return infos;
    }
    public void setInfos(List<String> infos) {
        this.infos = infos;
    }
}
