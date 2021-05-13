package com.bugcat.catface.spi;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * jackson json转对象泛型
 * @see AbstractResponesWrapper#getWrapperType(Type)
 * */
public abstract class CatTypeReference<T> {

    static ConcurrentMap<Type, Type> classTypeCache
            = new ConcurrentHashMap<Type, Type>(16, 0.75f, 1);

    
    protected final Type type;

    
    
    /**
     * new JackTypeReference<ResponseEntity<T>>(){};
     */
    protected CatTypeReference(){
        Type superClass = getClass().getGenericSuperclass();
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    
    /**
     * new JackTypeReference<ResponseEntity<T>>(type){};
     */
    protected CatTypeReference(Type... actualTypeArguments) {
        Class<?> thisClass = this.getClass();
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


    public Type getType() {
        return this.type;
    }
}
