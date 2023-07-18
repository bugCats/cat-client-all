package cc.bugcat.catface.spi;

import cc.bugcat.catface.annotation.CatNote;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

public interface CatClientBridge {

    
    
    /**
     * 从方法上获取{@code CatMethod}的notes
     * */
    default CatNote[] findCatNotes(Method method) {
        return new CatNote[0];
    }
    
    /**
     * 从interface上获取{@code RequestMapping}的value
     * */
    default String findBasePath(Class inter){
        RequestMapping requestMapping = AnnotationUtils.findAnnotation(inter, RequestMapping.class);
        if( requestMapping == null ){
            return "";
        }
        String[] value = requestMapping.value();
        return value.length > 0 ? value[0] : "";
    }
    
}
