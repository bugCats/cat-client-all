package cc.bugcat.example.catclient.sign;

import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.catclient.spi.SimpleCatClientFactory;

import java.util.function.Supplier;

public class SignFactory extends SimpleCatClientFactory {

    @Override
    public Supplier<CatSendProcessor> newSendHandler() {
        return () -> new SignSendProcessor();
    }

}
