package cc.bugcat.example.catclient.sign;

import cc.bugcat.catclient.handler.SendProcessor;
import cc.bugcat.catclient.spi.CatClientFactory;

public class SignFactory extends CatClientFactory {

    @Override
    protected SendProcessor sendHandler() {
        return new SignSendProcessor();
    }

}
