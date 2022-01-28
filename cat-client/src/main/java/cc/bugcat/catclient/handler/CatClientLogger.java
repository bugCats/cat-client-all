package cc.bugcat.catclient.handler;

import cc.bugcat.catface.utils.CatToosUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * http请求日志类
 * */
public class CatClientLogger {

    /**
     * 日志记录方案
     */
    private CatLogsMod logsMod;

    private long executeTime;       //调用http耗时

    private String errorMessge;             //错误信息，如果不为null，说明调用失败
    private CatHttpException exception;     //错误信息，如果不为null，说明调用失败

    private String apiName;     //方法名

    private String apiUrl;      //最终请求url

    private String request;     //输入报文

    private String response;    //响应报文


    private List<String> infos = new LinkedList<>();

    @Override
    public String toString(){

        Map<String, Object> logInfo = new LinkedHashMap<>();
        logInfo.put("@name", apiName);
        logInfo.put("@url", apiUrl);
        logInfo.put("@in", "#{in}");
        logInfo.put("@out", "#{out}");
        logInfo.put("@info", infos.toString());
        logInfo.put("@time", executeTime + "ms");

        boolean printIn = CatLogsMod.All == logsMod || CatLogsMod.In == logsMod;
        boolean printOut = CatLogsMod.All == logsMod || CatLogsMod.Out == logsMod;

        if( isFail() ){
            logInfo.put("@error", errorMessge != null ? errorMessge : String.valueOf(exception.getStatusCode()) + " - " + exception.getStatusText());

            printIn = !printIn && (CatLogsMod.All2 == logsMod || CatLogsMod.In2 == logsMod);
            printOut = !printOut && (CatLogsMod.All2 == logsMod || CatLogsMod.Out2 == logsMod);
        }

        String logs = JSONObject.toJSONString(logInfo)
                .replace("\"#{in}\"", printIn ? CatToosUtil.defaultIfBlank(request, "\"\"") : "\"\"")
                .replace("\"#{out}\"", printOut ? CatToosUtil.defaultIfBlank(response, "\"\"") : "\"\"");

        return logs;
    }


    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public boolean isSucc(){
        return exception == null && errorMessge == null;
    }

    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public boolean isFail(){
        return !isSucc();
    }

    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public CatClientLogger info(String info){
        infos.add(info);
        return this;
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
