package com.bugcat.example.catclient.sign;

import com.bugcat.catclient.handler.SendProcessor;
import com.bugcat.catclient.spi.CatClientFactory;
import org.springframework.stereotype.Component;

public class SignFactory extends CatClientFactory {

    @Override
    protected SendProcessor sendHandler() {
        return new SignSendProcessor();
    }

}
