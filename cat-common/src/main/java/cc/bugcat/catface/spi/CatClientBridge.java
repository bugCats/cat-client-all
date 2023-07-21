package cc.bugcat.catface.spi;

import cc.bugcat.catface.annotation.CatNote;

import java.lang.reflect.Method;

public interface CatClientBridge {

    
    
    /**
     * 从方法上获取{@code CatMethod}的notes
     * */
    default CatNote[] findCatNotes(Method method) {
        return new CatNote[0];
    }
    
    
}
