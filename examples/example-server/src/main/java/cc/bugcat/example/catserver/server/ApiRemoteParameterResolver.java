package cc.bugcat.example.catserver.server;

import cc.bugcat.catserver.handler.CatMethodInfo;
import cc.bugcat.catserver.spi.CatParameterResolver;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author bugcat
 */
@Component
public class ApiRemoteParameterResolver implements CatParameterResolver {

    @Override
    public Object[] resolveArguments(CatMethodInfo methodInfo, Object[] args) throws Exception {
        Method serverMethod = methodInfo.getServerMethod();
        Class<?> serverClass = serverMethod.getDeclaringClass();
        System.out.println("参数预处理:" + serverClass.getSimpleName()  + "." + serverMethod.getName() + ":" + JSONObject.toJSONString(args));
        return args;
    }
}
