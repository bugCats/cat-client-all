package cc.bugcat.catclient.spi;

import cc.bugcat.catclient.handler.CatClientContextHolder;
import cc.bugcat.catclient.handler.CatFastjsonResolver;
import cc.bugcat.catclient.handler.CatJacksonResolver;
import cc.bugcat.catface.spi.AbstractResponesWrapper;

import java.lang.reflect.Type;

/**
 * 对象序列化、反序列化
 * 
 * @see CatFastjsonResolver
 * @see CatJacksonResolver
 * @see CatSendProcessor#postVariableResolver(CatClientContextHolder)
 * @author bugcat
 * */
public interface CatPayloadResolver {

    <T> T toJavaBean(String text, Type type) throws Exception;

    <T> T toJavaBean(String text, AbstractResponesWrapper<T> wrapper, Type type) throws Exception;

    String toSendString(Object object) throws Exception;
    
}
