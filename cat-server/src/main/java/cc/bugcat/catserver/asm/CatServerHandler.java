package cc.bugcat.catserver.asm;

import cc.bugcat.catserver.scanner.CatControllerFactoryBean;

/**
 * 所有通过cat-server生成的服务端controller，一定是其子类。
 * 
 * */
public interface CatServerHandler {
    
    
    
    /**
     * 从cglib动态代理的controller中，获取对应CatServer class。
     * 
     * @see CatControllerFactoryBean#catServerClass
     * 
     * @return 被@CatServer标记的对象实例
     * */
    public Class getCatServerClass();
    
    
}
