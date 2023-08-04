package cc.bugcat.catclient.beanInfos;

import cc.bugcat.catclient.annotation.CatMethod;
import cc.bugcat.catface.annotation.CatNote;
import cc.bugcat.catface.spi.CatClientBridge;
import cc.bugcat.catface.utils.CatToosUtil;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;

public class CatMethodBridge implements CatClientBridge {


    @Override
    public CatNote[] findCatNotes(Method method) {
        CatMethod catMethod = AnnotationUtils.findAnnotation(method, CatMethod.class);
        if( catMethod != null ){
            return catMethod.notes();
        } else {
            return new CatNote[0];
        }
    }

    @Override
    public String findBasePath(Class inter) {
        String basePath = null;
        CatMethod catMethod = AnnotationUtils.findAnnotation(inter, CatMethod.class);
        if( catMethod != null && CatToosUtil.isNotBlank(basePath = catMethod.value())){
            return basePath;
        }
        basePath = CatClientBridge.super.findBasePath(inter);
        return basePath;
    }
}
