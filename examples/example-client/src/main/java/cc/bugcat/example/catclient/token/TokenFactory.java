package cc.bugcat.example.catclient.token;

import cc.bugcat.catclient.handler.CatSendProcessor;
import cc.bugcat.catclient.handler.DefaultCatClientFactory;
import cc.bugcat.catclient.spi.CatHttp;
import cc.bugcat.catclient.utils.CatRestHttp;

public class TokenFactory extends DefaultCatClientFactory {

    @Override
    public CatSendProcessor newSendHandler() {
        return new TokenSendProcessor();
    }

    @Override
    public CatHttp getCatHttp() {
        return new CatRestHttp();
    }


}
