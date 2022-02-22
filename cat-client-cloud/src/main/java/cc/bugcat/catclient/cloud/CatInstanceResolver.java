package cc.bugcat.catclient.cloud;

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

    private Matcher matcher;

    /**
     * 服务名
     * */
    private String serviceName;     //服务名

    /**
     * ip和端口
     * */
    private String ipAddr;

    /**
     * 全部替换好后可以直接使用的实例地址：http://ip:port/ctx/url
     * */
    private String sendPath;



    /**
     * @param lbPath 原始的url，包含cloud服务名 http://serviceName/ctx/url
     * */
    public CatInstanceResolver(String lbPath){
        this.matcher = lbNamePattern.matcher(lbPath);
        if ( matcher.find() ) {
            this.serviceName = matcher.group(2);
        } else {
            throw new IllegalArgumentException("非法的负载均衡表达式：" + lbPath + "，格式应该为：http[s]://lbname[/ctx/url?k=v]");
        }
    }



    /**
     * 从注册中心获取的ip+端口，填充到请求地址中
     * @return "ip:port" or "www.bugcat.cc" or "http://bugcat.cc/github"
     * */
    public String resolver(String ipAddr){
        this.ipAddr = ipAddr;
        this.sendPath = ipAddr.startsWith("http") ? matcher.replaceAll(ipAddr + "$3") : matcher.replaceAll("$1" + ipAddr + "$3");
        return sendPath;
    }



    public String getServiceName() {
        return serviceName;
    }
    public String getIpAddr() {
        return ipAddr;
    }
    public String getSendPath() {
        return sendPath;
    }




}
