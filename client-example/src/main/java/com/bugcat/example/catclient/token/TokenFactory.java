package com.bugcat.example.catclient.token;

import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.catclient.spi.CatClientFactory;
import com.bugcat.catclient.spi.CatHttp;
import com.bugcat.catclient.utils.CatHttpUtil;

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
