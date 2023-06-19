package cc.bugcat.catface.handler;

import cc.bugcat.catface.annotation.CatResponesWrapper;
import cc.bugcat.catface.annotation.Catface;


public class CatApiInfo {

    
    private CatResponesWrapper wrapper;
    
    private Catface catface;


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

}
