package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.config.CatClientConfiguration;
import cc.bugcat.catclient.spi.CatPayloadResolver;
import cc.bugcat.catclient.utils.CatClientUtil;
import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catface.spi.CatTypeReference;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;

/**
 * jackson 序列化与反序列化
 * 默认序列化工具，可在{@link CatClientConfiguration#getPayloadResolver()}指定
 *
 * @author bugcat
 * */
public class CatJacksonResolver implements CatPayloadResolver {


    private ObjectMapper mapper;


    public CatJacksonResolver(){
        this(null);
    }

    public CatJacksonResolver(ObjectMapper objectMapper){
        mapper = objectMapper;
        if( mapper == null ){
            mapper = CatClientUtil.getBean(ObjectMapper.class);
        }
        if( mapper == null ){
            mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        }
    }



    @Override
    public <T> T toJavaBean(String text, Type type) throws Exception {
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        T object = mapper.readValue(text, javaType);
        return object;
    }


    @Override
    public <T> T toJavaBean(String text, AbstractResponesWrapper<T> wrapper, Type type) throws Exception {
        CatTypeReference typeRef = wrapper.getWrapperType(type);
        JavaType javaType = mapper.getTypeFactory().constructType(typeRef.getType());
        T object = mapper.readValue(text, javaType);
        return object;
    }


    @Override
    public String toSendString(Object object) throws Exception {
        return mapper.writeValueAsString(object);
    }

}
