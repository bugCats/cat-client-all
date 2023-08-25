package cc.bugcat.catclient.handler;

import cc.bugcat.catclient.spi.CatClientMockProvide;

import java.util.HashSet;
import java.util.Set;

public class CatClientMockProvideBuilder {

    public CatClientMockProvideBuilder(){
        
    }
    
    private Set<Class> clientMocks = new HashSet<>();
    
    
    public CatClientMockProvideBuilder mockClient(Class interfaceClass){
        clientMocks.add(interfaceClass);
        return this;
    }
    
    public CatClientMockProvide build(){
        return new CatClientMockProvide() {
            @Override
            public boolean enableMock() {
                return true;
            }

            @Override
            public Set<Class> mockClients() {
                return clientMocks;
            }
        };
    }
    
    
    
}
