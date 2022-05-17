package cc.bugcat.catclient.cloud;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 实例对象处理
 *
 * http请求路径格式为：http[s]://lbname[/ctx]
 * 其中lbname为服务名
 *
 * @author bugcat
 * */
public final class CatInstanceResolver {

    /**
     * 截取服务名
     * */
    private static final Pattern lbNamePattern = Pattern.compile("^(https?://)([^/]+)(/?)");

    /**
     * 自定义属性
     * */
    private final Map<String, Object> attributesMap = new HashMap<>();
    
    
    private final Matcher matcher;

    /**
     * 服务名
     * */
    private final String serviceName;     //服务名

    /**
     * ip和端口
     * */
    private String ipAddr;

    /**
     * 全部替换好后可以直接使用的实例地址：http://ip:port/ctx
     * */
    private String realHost;



    /**
     * @param lbName 原始的负载均衡表达式，包含cloud服务名 http://serviceName/ctx
     * */
    protected CatInstanceResolver(String lbName){
        this.matcher = lbNamePattern.matcher(lbName);
        if ( matcher.find() ) {
            this.serviceName = matcher.group(2);
        } else {
            throw new IllegalArgumentException("非法的负载均衡表达式：" + lbName + "，格式应该为：http[s]://lbname[/ctx]");
        }
    }



    /**
     * 从注册中心获取的ip端口，填充到请求地址中
     * @param ipAddr 从注册中心中获取的实例ip端口
     * @return "http://ip:port/ctx" or "http://www.bugcat.cc/ctx" or "https://bugcat.cc/ctx"
     * */
    protected String resolver(String ipAddr){
        this.ipAddr = ipAddr;
        this.realHost = ipAddr.startsWith("http") ? matcher.replaceAll(ipAddr + "$3") : matcher.replaceAll("$1" + ipAddr + "$3");
        return realHost;
    }

    protected String getIpAddr() {
        return ipAddr;
    }
    
    protected String getRealHost() {
        return realHost;
    }

    

    public String getServiceName() {
        return serviceName;
    }

    public CatInstanceResolver putAttributes(String name, Object value){
        attributesMap.put(name, value);
        return this;
    }
    
    public Object getAttributes(String name){
        return getAttributes(name, null);
    }
    
    public Object getAttributes(String name, Object defaultValue){
        return attributesMap.getOrDefault(name, defaultValue);
    }
    
    public Map<String, Object> getAttributesMap() {
        return attributesMap;
    }
}
