package cc.bugcat.example.catclient.sign;

import cc.bugcat.catclient.handler.CatSendProcessor;
import cc.bugcat.catclient.handler.DefaultCatClientFactory;

public class SignFactory extends DefaultCatClientFactory {

    @Override
    public CatSendProcessor newSendHandler() {
        return new SignSendProcessor();
    }

}
