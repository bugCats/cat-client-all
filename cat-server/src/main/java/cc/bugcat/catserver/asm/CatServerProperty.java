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


    /**
     * 被@CatServer标记的class
     * */
    private final Class serverClass;

    /**
     * 被@CatServer标记的对象实例。
     * serverBean.getClass() ≠ serverClass
     * */
    private final Object serverBean;
    
    /**
     * CatServer标记信息
     * */
    private final CatServerInfo serverInfo;

    public CatServerProperty(Class serverClass, Object serverBean, CatServerInfo serverInfo) {
        this.serverClass = serverClass;
        this.serverBean = serverBean;
        this.serverInfo = serverInfo;
    }

    public Class getServerClass() {
        return serverClass;
    }

    public Object getServerBean() {
        return serverBean;
    }

    public CatServerInfo getServerInfo() {
        return serverInfo;
    }
}
