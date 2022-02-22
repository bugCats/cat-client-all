package cc.bugcat.catface.spi;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * jackson、fastjson 序列化与反序列化泛型处理
 *
 * @see AbstractResponesWrapper#getWrapperType(Type)
 * */
public abstract class CatTypeReference<T> {

    protected static ConcurrentMap<Type, Type> classTypeCache = new ConcurrentHashMap<Type, Type>(16, 0.75f, 1);


    protected final Type type;


    /**
     * 对于有泛型的类反序列化方式：
     * new JackTypeReference<ResponseEntity<Demo>>(){};
     * */
    protected CatTypeReference(){
        Type superClass = getClass().getGenericSuperclass();
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }


    /**
     * 对于有泛型的类，并且泛型是变量情况，反序列化方式：
     * 1、new JackTypeReference<ResponseEntity<T>>(Demo){};
     * 2、new JackTypeReference<Map<S, T>>(String, Demo){};
     *
     * @param actualTypeArguments 实际的泛型Type。顺序与泛型占位符一致
     * */
    protected CatTypeReference(Type... actualTypeArguments) {
        Class thisClass = this.getClass();
        Type superClass = thisClass.getGenericSuperclass();
        ParameterizedType argType = (ParameterizedType) ((ParameterizedType) superClass).getActualTypeArguments()[0];
        Type rawType = argType.getRawType();
        Type[] argTypes = argType.getActualTypeArguments();

        int actualIndex = 0;
        for (int i = 0; i < argTypes.length; ++i) {
            if (argTypes[i] instanceof TypeVariable ) {
                argTypes[i] = actualTypeArguments[actualIndex++];
                if (actualIndex >= actualTypeArguments.length) {
                    break;
                }
            }
        }
        Type key = ParameterizedTypeImpl.make((Class)rawType, argTypes, thisClass);
        Type cachedType = classTypeCache.get(key);
        if (cachedType == null) {
            classTypeCache.putIfAbsent(key, key);
            cachedType = classTypeCache.get(key);
        }
        type = cachedType;
    }


    /**
     * 反序列化对象的Type。包含了泛型Type
     * */
    public Type getType() {
        return this.type;
    }

}
