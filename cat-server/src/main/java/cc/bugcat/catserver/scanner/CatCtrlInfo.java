package cc.bugcat.catserver.scanner;

import cc.bugcat.catserver.handler.CatServerInfo;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * controller对象工厂。
 * 先增强interface，然后根据增强后的Interface使用cglib动态生成controller对象。
 *
 * @author bugcat
 * */
class CatCtrlInfo {

    /**
     * {@link @CatServer}注解信息
     * */
    private final CatServerInfo serverInfo;

    
    /**
     * 通过动态代理生成的controller对象
     * */
    private final Object controller;

    
    /**
     * CatServer类的实现方法
     * */
    private final Set<Method> bridgeMethods;
    
    
    
    protected CatCtrlInfo(CatCtrlInfoBuilder builder) {
        this.serverInfo = builder.serverInfo;
        this.controller = builder.controller;
        this.bridgeMethods = builder.bridgeMethods;
    }


    public CatServerInfo getServerInfo() {
        return serverInfo;
    }
    public Object getController() {
        return controller;
    }
    public Set<Method> getBridgeMethods() {
        return bridgeMethods;
    }


}
