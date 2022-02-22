package cc.bugcat.example.catclient.sign;

import cc.bugcat.catclient.spi.CatSendProcessor;
import cc.bugcat.catclient.spi.DefaultCatClientFactory;

import java.util.function.Supplier;

public class SignFactory extends DefaultCatClientFactory {

    @Override
    public Supplier<CatSendProcessor> newSendHandler() {
        return () -> new SignSendProcessor();
    }

}
