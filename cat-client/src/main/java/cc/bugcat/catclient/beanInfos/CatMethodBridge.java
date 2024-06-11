package cc.bugcat.catclient.beanInfos;

import cc.bugcat.catclient.annotation.CatMethod;
import cc.bugcat.catface.annotation.CatNote;
import cc.bugcat.catface.spi.CatClientBridge;
import cc.bugcat.catface.utils.CatToosUtil;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Annotation;
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


    @Override
    public RequestMapping findMethodPath(Method method) {
        CatMethod catMethod = AnnotationUtils.findAnnotation(method, CatMethod.class);
        if( catMethod != null && CatToosUtil.isNotBlank(catMethod.value())){
            return adapter(catMethod);
        }
        return CatClientBridge.super.findMethodPath(method);
    }
    
    
    private RequestMapping adapter(CatMethod catMethod){
        return new RequestMapping(){
            @Override
            public String name() {
                return "";
            }
            @Override
            public String[] value() {
                return new String[]{catMethod.value()};
            }
            @Override
            public String[] path() {
                return new String[]{catMethod.value()};
            }
            @Override
            public RequestMethod[] method() {
                return new RequestMethod[]{catMethod.method()};
            }
            @Override
            public String[] params() {
                return new String[0];
            }
            @Override
            public String[] headers() {
                return new String[0];
            }
            @Override
            public String[] consumes() {
                return new String[0];
            }
            @Override
            public String[] produces() {
                return new String[0];
            }
            @Override
            public Class<? extends Annotation> annotationType() {
                return RequestMapping.class;
            }
        };
    }
}
