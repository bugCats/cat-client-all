package cc.bugcat.catserver.spi;

import cc.bugcat.catserver.annotation.CatBefore;
import cc.bugcat.catserver.handler.CatMethodInfo;


/**
 * controller入参预处理
 *
 * @see CatBefore
 * @author bugcat
 * */
public interface CatParameterResolver {


    default Object[] resolveArguments(CatMethodInfo methodInfo, Object[] args) throws Exception {
        return args;
    }


}
