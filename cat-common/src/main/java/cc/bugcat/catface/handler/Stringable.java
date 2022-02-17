package cc.bugcat.catface.handler;

/**
 * 使用post发送字符串时，如果入参实现了Stringable，那么直接使用对象内置的序列化。
 * */
public interface Stringable {


    String serialization();

}
