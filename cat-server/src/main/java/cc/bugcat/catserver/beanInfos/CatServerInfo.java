package cc.bugcat.catserver.beanInfos;

import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.catface.annotation.Catface;
import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.annotation.CatServer;
import cc.bugcat.catserver.spi.CatInterceptor;

import java.util.Map;

public class CatServerInfo {


    private final Class warpClass;      //响应包装器类
    private final AbstractResponesWrapper warp;      //响应包装器类
    
    private final Class<? extends CatInterceptor>[] handers;

    private final Catface catface;  //是否使用精简模式
    private final boolean isCatface;
    
    private CatServerInfo(CatServer catServer, Map<String, Object> paramMap) {

        this.handers = catServer.handers();
        
        //响应包装器类，如果是ResponesWrapper.default，代表没有设置
        CatResponesWrapper responesWrapper = (CatResponesWrapper) paramMap.get("wrapper");
        Class<? extends AbstractResponesWrapper> wrapper = responesWrapper == null ? null : responesWrapper.value();
        this.warp = wrapper == null || AbstractResponesWrapper.Default.class.equals(wrapper) ? null : AbstractResponesWrapper.getResponesWrapper(wrapper);
        this.warpClass = warp == null ? null : warp.getWrapperClass();
        
        this.catface = (Catface) paramMap.get("catface");
        this.isCatface = catface != null;
    }


    public final static CatServerInfo buildServerInfo(Class serverClass) {
        CatServer catServer = (CatServer) serverClass.getAnnotation(CatServer.class);
        Map<String, Object> paramMap = CatToosUtil.getAttributes(serverClass);
        CatServerInfo serverInfo = new CatServerInfo(catServer, paramMap);
        return serverInfo;
    }

    public Class getWarpClass() {
        return warpClass;
    }
    public AbstractResponesWrapper getWarp() {
        return warp;
    }
    public Class<? extends CatInterceptor>[] getHanders() {
        return handers;
    }
    public Catface getCatface() {
        return catface;
    }
    public boolean isCatface() {
        return isCatface;
    }
}
