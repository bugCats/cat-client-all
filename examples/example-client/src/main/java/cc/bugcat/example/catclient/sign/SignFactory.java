package cc.bugcat.example.catclient.sign;

import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.catclient.spi.SimpleClientFactory;

import java.util.function.Supplier;

public class SignFactory extends SimpleClientFactory {

    @Override
    public Supplier<CatSendProcessor> newSendHandler() {
        return () -> new SignSendProcessor();
    }

}
