package com.bugcat.catclient.cloud;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CatInstanceEntry{

    /**
     * 截取服务名
     */
    private static Pattern lbNamePattern = Pattern.compile("^(https?://)([^/]+)(/?)");

    private String serviceName;     //服务名
    private String ipAddr;          //ip:port
    private String hostAddr;        //全部替换好后可以直接使用的实例地址：http://ip:port/ctx

    private Matcher matcher;
    
    public CatInstanceEntry(String lbName){
        this.matcher = lbNamePattern.matcher(lbName);
        if ( matcher.find() ) {
            this.serviceName = matcher.group(2);
        } else {
            throw new IllegalArgumentException("非法的负载均衡表达式：" + lbName + "，格式应该为：http[s]://lbname[/ctx]");
        }
    }

    public String getServiceName() {
        return serviceName;
    }
    public String getIpAddr() {
        return ipAddr;
    }
    public String getHostAddr() {
        return hostAddr;
    }
    
    
    void setIpAddr(String ipAddr){
        this.ipAddr = ipAddr;
        this.hostAddr = matcher.replaceAll("$1" + ipAddr + "$3");
    }
    
}
