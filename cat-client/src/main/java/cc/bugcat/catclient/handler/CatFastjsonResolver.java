package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.spi.CatPayloadResolver;
import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catface.spi.CatTypeReference;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Type;

/**
 * fastjson 序列化与反序列化
 *
 * @author bugcat
 * */
public class CatFastjsonResolver implements CatPayloadResolver {


    @Override
    public <T> T toJavaBean(String text, Type type) {
        return JSONObject.parseObject(text, type);
    }


    @Override
    public <T> T toJavaBean(String text, AbstractResponesWrapper<T> wrapper, Type type) {
        CatTypeReference typeRef = wrapper.getWrapperType(type);
        return JSONObject.parseObject(text, typeRef.getType());
    }


    @Override
    public String toSendString(Object object) {
        return JSONObject.toJSONString(object);
    }
    
}
