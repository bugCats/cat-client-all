package cc.bugcat.catface.handler;

/**
 * 使用post发送字符串时，如果入参对象实现了Stringable，那么直接使用对象内置的序列化。
 *
 * @author bugcat
 * */
public interface Stringable {


    String serialization();

}
