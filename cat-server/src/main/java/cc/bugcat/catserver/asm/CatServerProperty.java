package cc.bugcat.catserver.asm;

import cc.bugcat.catserver.beanInfos.CatServerInfo;

import java.lang.reflect.Method;
import java.util.function.Function;


/**
 * 动态生成的controller包含的属性
 * */
public class CatServerProperty {




    public static String serverPropertyMethodName(){
        for ( Method method : CatServerInstance.class.getMethods() ) {
            return method.getName();
        }
        return null;
    }
    
    
    
    private final Class serverClass;
    private final CatServerInfo serverInfo;

    public CatServerProperty(Class serverClass, CatServerInfo serverInfo) {
        this.serverClass = serverClass;
        this.serverInfo = serverInfo;
    }

    public Class getServerClass() {
        return serverClass;
    }

    public CatServerInfo getServerInfo() {
        return serverInfo;
    }
}
