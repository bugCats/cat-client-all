package com.bugcat.catclient.spi;

/**
 * 使用post发送字符串
 * 如果入参实现了Stringable，那么直接使用对象内置的序列化
 * */
public interface Stringable {

    
    String toString();
    
    
    default String serialize() {
        return toString();
    }
    
}
