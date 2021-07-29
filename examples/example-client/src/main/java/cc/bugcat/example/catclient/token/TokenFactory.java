package cc.bugcat.example.catclient.token;

import cc.bugcat.catclient.handler.SendProcessor;
import cc.bugcat.catclient.spi.CatClientFactory;
import cc.bugcat.catclient.spi.CatHttp;
import cc.bugcat.catclient.utils.CatHttpUtil;

public class TokenFactory extends CatClientFactory {

    @Override
    protected SendProcessor sendHandler() {
        return new TokenSendProcessor();
    }

    @Override
    protected CatHttp catHttp() {
        return new CatHttpUtil();
    }
}
