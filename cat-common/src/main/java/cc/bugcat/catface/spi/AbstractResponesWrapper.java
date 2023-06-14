package cc.bugcat.catface.spi;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * http响应包装器类处理。
 *
 * 部分框架，服务端的响应，为统一对象，具体的业务数据，是统一对象通过泛型确认的属性。
 *
 * 比喻{@code ResponseEntity&lt;User&gt;} {@code HttpEntity&lt;User&gt;}，{@code ResponesDTO&lt;User&gt;}，具体响应是通过泛型 T 确认的一个属性。
 *
 * 可以称这类ResponseEntity、HttpEntity、ResponesDTO为响应包装器类
 *
 * 1、对于cat-client，此处可以设置去包装器类，从而interface的方法，可以直接返回具体的业务对象
 *
 * 2、对于cat-server，可以设置自动加包装器类，让所有的响应，都封装成统一的响应包装器类
 *
 * 拆包装、加包装
 *
 *
 *
 * @author bugcat
 * */
public abstract class AbstractResponesWrapper<T> {

    /**
     * 静态方法调用
     * */
    public final static synchronized AbstractResponesWrapper getResponesWrapper(Class<? extends AbstractResponesWrapper> wrapperClass, Consumer<AbstractResponesWrapper> consumer){
        try {
            AbstractResponesWrapper wrapper = wrapperClass.newInstance();
            consumer.accept(wrapper);
            return wrapper;
        } catch ( Exception e ) {
            throw new RuntimeException("获取" + wrapperClass.getSimpleName() + "包装器类异常");
        }
    }


    /**
     * 获取包装器类class
     * */
    public abstract Class<T> getWrapperClass();


    /**
     * 获取json转对象泛型：new CatTypeReference&lt;ResponseEntity&lt;T&gt;&gt;(type){};
     * @see CatTypeReference
     *
     * @param type 业务类型的Type。其中，ResponseEntity 为包装器类
     * */
    public abstract <T> CatTypeReference<T> getWrapperType(Type type);


    /**
     * 校验业务处理结果
     * 直接抛出异常
     * */
    public abstract void checkValid(T wrapper) throws Exception;


    /**
     * 得到业务数据
     * */
    public abstract Object getValue(T wrapper);


    /**
     * 成功时构建
     * */
    public abstract T createEntryOnSuccess(Object value, Class methodReturnClass);


    /**
     * 当异常时构建
     * */
    public abstract T createEntryOnException(Throwable throwable, Class methodReturnClass);


    /**
     * 默认值
     * */
    public final static class Default extends AbstractResponesWrapper<Object>{
        @Override
        public Class<Object> getWrapperClass() {
            return null;
        }
        @Override
        public CatTypeReference getWrapperType(Type type) {
            return null;
        }
        @Override
        public void checkValid(Object wrapper) throws Exception {}
        @Override
        public Object getValue(Object wrapper) {
            return null;
        }
        @Override
        public Object createEntryOnSuccess(Object value, Class methodReturnClass) {
            return value;
        }
        @Override
        public Object createEntryOnException(Throwable throwable, Class methodReturnClass) {
            throwable.printStackTrace();
            return null;
        }
    }

}
