package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.spi.CatPayloadResolver;
import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catface.utils.CatToosUtil;

import java.lang.reflect.Type;

class CatPayloadResolverDecorator implements CatPayloadResolver {

    
    private final CatPayloadResolver payloadResolver;

    CatPayloadResolverDecorator(CatPayloadResolver payloadResolver) {
        this.payloadResolver = payloadResolver;
    }


    @Override
    public <T> T toJavaBean(String text, Type type) {
        try {
            return payloadResolver.toJavaBean(text, type);
        } catch ( Exception ex ) {
            throw new PayloadResolverException("对象反序列化异常：" + ex.getMessage(), CatToosUtil.getCause(ex));
        }
    }

    @Override
    public <T> T toJavaBean(String text, AbstractResponesWrapper<T> wrapper, Type type) {
        try {
            return payloadResolver.toJavaBean(text, wrapper, type);
        } catch ( Exception ex ) {
            throw new PayloadResolverException("对象反序列化异常：" + ex.getMessage(), CatToosUtil.getCause(ex));
        }
    }

    @Override
    public String toSendString(Object object) {
        try {
            return payloadResolver.toSendString(object);
        } catch ( Exception ex ) {
            throw new PayloadResolverException("对象序列化异常：" + ex.getMessage(), CatToosUtil.getCause(ex));
        }
    }
}
