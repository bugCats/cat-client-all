package cc.bugcat.catface.handler;

import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.catface.annotation.Catface;
import org.springframework.web.bind.annotation.RequestMapping;


public class CatApiInfo {

    /**
     * 包装器类
     * */
    private CatResponesWrapper wrapper;
    
    /**
     * 是否为catface模式
     * */
    private Catface catface;

    /**
     * 在interface上的RequestMapping注解
     * */
    private RequestMapping requestMapping;
    
    
    
    public CatResponesWrapper getWrapper() {
        return wrapper;
    }
    public void setWrapper(CatResponesWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public Catface getCatface() {
        return catface;
    }
    public void setCatface(Catface catface) {
        this.catface = catface;
    }

    public RequestMapping getRequestMapping() {
        return requestMapping;
    }
    public void setRequestMapping(RequestMapping requestMapping) {
        this.requestMapping = requestMapping;
    }
}
