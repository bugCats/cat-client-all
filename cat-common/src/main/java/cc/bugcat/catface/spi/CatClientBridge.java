package cc.bugcat.catface.spi;

import cc.bugcat.catface.annotation.CatNote;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.ServiceLoader;

public interface CatClientBridge {

    static CatClientBridge loadService() {
        final ServiceLoader<CatClientBridge> loaders = ServiceLoader.load(CatClientBridge.class);
        final Iterator<CatClientBridge> iterators = loaders.iterator();
        if (iterators.hasNext()) {
            return iterators.next();
        }
        return new CatClientBridge() {};
    }
    
    
    /**
     * 从方法上获取{@code @CatMethod}的notes
     * */
    default CatNote[] findCatNotes(Method method) {
        return new CatNote[0];
    }
    
}
