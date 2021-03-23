package com.bugcat.catserver.handler;

import com.bugcat.catface.spi.ResponesWrapper;
import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.catserver.beanInfos.CatServerInfo;
import com.bugcat.catserver.spi.CatInterceptor;
import com.bugcat.catserver.utils.CatServerUtil;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 通过cglib生成代理类
 *
 * @author bugcat
 */
public final class CatInterceptorBuilders {

    private CatInterceptorBuilders() { }
    
    private Map<String, MethodBuilder> builderMap = new HashMap<>();

    public static CatInterceptorBuilders builders(){
        return new CatInterceptorBuilders();
    }


    public MethodBuilder builder(Method method, boolean isBridgeMethod){
        String methodSign = isBridgeMethod ? CatToosUtil.signature(CatServerUtil.trimMethodName(method.getName()), method) : CatToosUtil.signature(method);
        MethodBuilder builder = builderMap.get(methodSign);
        if( builder == null ){
            builder = new MethodBuilder();
            builderMap.put(methodSign, builder);
        }
        return builder;
    }
    
    
    public MethodBuilder getBuilder(Method method){
        String methodSign = null;
        if( CatServerUtil.isBridgeMethod(method)){
            methodSign = CatToosUtil.signature(CatServerUtil.trimMethodName(method.getName()), method);
        } else {
            methodSign = CatToosUtil.signature(method);
        }
        return builderMap.get(methodSign);
    }



    public final static class MethodBuilder {
        
        private StandardMethodMetadata interMethod;         //interface上对于的桥连方法
        private Method realMethod;                          //原始方法
        private List<CatInterceptor> handers;
        private ResponesWrapper wrapper;
        
        
        private MethodBuilder(){}

        public MethodBuilder interMethod(StandardMethodMetadata interMethod) {
            this.interMethod = interMethod;
            return this;
        }
        public MethodBuilder realMethod(Method realMethod) {
            this.realMethod = realMethod;
            return this;
        }
        
        
        public CatMethodInterceptor getCatMethodInterceptor(CatServerInfo catServerInfo){
            Class<? extends CatInterceptor>[] handerList = catServerInfo.getHanders();
            handers = new ArrayList<>(handerList.length);
            for(Class<? extends CatInterceptor> clazz : handerList) {
                if( CatInterceptor.class.equals(clazz) ){
                    handers.add(CatInterceptor.instance);
                } else {
                    handers.add(CatServerUtil.getBean(clazz));
                }
            }
            handers.sort(Comparator.comparingInt(CatInterceptor::getOrder));
            wrapper = ResponesWrapper.getResponesWrapper(catServerInfo.getWrapper());
            CatMethodInterceptor interceptor = new CatMethodInterceptor(this);
            return interceptor;
        }

        
        
        public StandardMethodMetadata getInterMethod() {
            return interMethod;
        }

        public Method getRealMethod() {
            return realMethod;
        }

        public List<CatInterceptor> getHanders() {
            return handers;
        }

        public ResponesWrapper getWrapper() {
            return wrapper;
        }
    }

    
    
}
