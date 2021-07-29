package cc.bugcat.catclient.spi;

import cc.bugcat.catface.spi.AbstractResponesWrapper;

import java.lang.reflect.Type;

/**
 * 对象序列化、反序列化
 * */
public interface CatJsonResolver {

    <T> T toJavaBean(String text, Type type);

    <T> T toJavaBean(String text, AbstractResponesWrapper<T> wrapper, Type type);

    String toJsonString(Object object);

    String toXmlString(Object object);
    
    
}
