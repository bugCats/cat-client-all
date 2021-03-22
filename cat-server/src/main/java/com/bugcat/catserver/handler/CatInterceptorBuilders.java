package com.bugcat.catserver.handler;

import com.bugcat.catface.spi.ResponesWrapper;
import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.catserver.beanInfos.CatServerInfo;
import com.bugcat.catserver.spi.CatInterceptor;
import com.bugcat.catserver.utils.CatServerUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 通过cglib生成代理类
 *
 * @author bugcat
 */
public final class CatInterceptorBuilders implements InitializingBean{
    
    private Map<String, MethodBuilder> builderMap = new HashMap<>();


    private CatServerInfo catServerInfo;

    
    public static CatInterceptorBuilders builders(){
        return new CatInterceptorBuilders();
    }

    private CatInterceptorBuilders() { }
    
    
    /**
     * 执行此方法之后，加入到组件初始化队列中
     * */
    public void registryInitializingBean(CatServerInfo catServerInfo) {
        this.catServerInfo = catServerInfo;
        CatServerUtil.addInitBean(this);
    }
    
    /**
     * 在组件初始化时执行，为builder赋值，并且初始化
     * */
    @Override
    public void afterPropertiesSet() throws Exception {
        Collection<MethodBuilder> builders = builderMap.values();
        for (MethodBuilder builder : builders ) {
            Class<? extends CatInterceptor>[] handerList = catServerInfo.getHanders();
            List<CatInterceptor> handers = new ArrayList<>(handerList.length);
            for(Class<? extends CatInterceptor> clazz : handerList) {
                if( CatInterceptor.class.equals(clazz) ){
                    handers.add(CatInterceptor.instance);
                } else {
                    handers.add(CatServerUtil.getBean(clazz));
                }
            }
            handers.sort(Comparator.comparingInt(CatInterceptor::getOrder));

            ResponesWrapper wrapper = ResponesWrapper.getResponesWrapper(catServerInfo.getWrapper());

            builder.handers = handers;
            builder.wrapper = wrapper;
            
            builder.catMethodInterceptor.initializing(builder);
        }
    }



    /**
     * MethodBuilder.builder 只会生成一个CatMethodInterceptor，具体初始化功能，在{@link CatInterceptorBuilders#afterPropertiesSet()}此处执行
     * */
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
        
        private CatMethodInterceptor catMethodInterceptor;
        
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
        
        
        public CatMethodInterceptor build(){
            return catMethodInterceptor == null ? catMethodInterceptor = new CatMethodInterceptor() : catMethodInterceptor;
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
