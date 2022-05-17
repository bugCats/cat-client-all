package cc.bugcat.catclient.spi;

import cc.bugcat.catclient.handler.CatFastjsonResolver;
import cc.bugcat.catclient.handler.CatJacksonResolver;
import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catclient.handler.CatSendContextHolder;

import java.lang.reflect.Type;

/**
 * 对象序列化、反序列化
 * 
 * @see CatFastjsonResolver
 * @see CatJacksonResolver
 * @see CatSendProcessor#postVariableResolver(CatSendContextHolder)
 * @author bugcat
 * */
public interface CatJsonResolver {

    <T> T toJavaBean(String text, Type type);

    <T> T toJavaBean(String text, AbstractResponesWrapper<T> wrapper, Type type);

    String toSendString(Object object);
    
}
