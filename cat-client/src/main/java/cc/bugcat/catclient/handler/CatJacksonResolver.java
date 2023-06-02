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
    public <T> T toJavaBean(String jsonString, Type type) {
        try {
            JavaType javaType = mapper.getTypeFactory().constructType(type);
            return mapper.readValue(jsonString, javaType);
        } catch ( Exception ex ) {
            throw new RuntimeException("对象反序列化异常：" + ex.getMessage(), ex);
        }
    }


    @Override
    public <T> T toJavaBean(String jsonString, AbstractResponesWrapper<T> wrapper, Type type) {
        CatTypeReference typeRef = wrapper.getWrapperType(type);
        try {
            JavaType javaType = mapper.getTypeFactory().constructType(typeRef.getType());
            return mapper.readValue(jsonString, javaType);
        } catch ( Exception ex ) {
            throw new RuntimeException("对象反序列化异常：" + ex.getMessage(), ex);
        }
    }


    @Override
    public String toSendString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch ( Exception ex ) {
            throw new RuntimeException("对象序列化异常", ex);
        }
    }

}
