package cc.bugcat.catserver.asm;

/**
 * 所有通过cat-server生成的服务端controller，一定是其子类。
 * */
public interface CatServerInstance {
    
    /**
     * 从cglib动态代理的controller中，获取对应CatServer class、代理对象、CatServerInfo等。
     * */
    CatServerProperty getServerProperty();

}
