package com.bugcat.catserver.scanner;

import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.catserver.utils.CatServerUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

/**
 * @author: bugcat
 * */
public class CatServerInitBean implements InitializingBean {

    public static final String bridgeName = "$bugcat$";
    public static final String annName = RequestMapping.class.getName();

    
    private List<Class> catServerList;
    private BeanDefinitionRegistry registry;
    
    
    @Override
    public void afterPropertiesSet() throws Exception {

        if( catServerList == null ){
            return;
        }
        
        List<BeanInfo> beanInfos = new ArrayList<>(catServerList.size());
        for(Class clazz : catServerList){
            BeanInfo info = new BeanInfo(clazz);
            beanInfos.add(info);
        }
        beanInfos.sort(BeanInfo::compareTo);
        IntFunction<RequestMethod[]> requestMethodToArray = RequestMethod[]::new;
        IntFunction<String[]> stringToArray = String[]::new;

        RequestMappingHandlerMapping mapper = CatServerUtil.getBean(RequestMappingHandlerMapping.class);
        for( BeanInfo info : beanInfos ){
            info.thisMethods.forEach((sign, metadata) -> {

                Method method = metadata.getIntrospectedMethod();
                Method superMethod = info.superMethods.get(bridgeName + method.getName());
                if( superMethod != null ){

                    Map<String, Object> attr = metadata.getAnnotationAttributes(annName);
                    RequestMappingInfo mappingInfo = RequestMappingInfo
                            .paths(getValue(attr, "value", stringToArray))
                            .methods(getValue(attr, "method", requestMethodToArray))
                            .params(getValue(attr, "params", stringToArray))
                            .headers(getValue(attr, "headers", stringToArray))
                            .produces(getValue(attr, "produces", stringToArray))
                            .consumes(getValue(attr, "consumes", stringToArray))
                            .build();

                    mapper.unregisterMapping(mappingInfo);
                    mapper.registerMapping(mappingInfo, info.bean, superMethod); // 注册映射处理
                }  
                
            });
        }
    }

    
    
    private class BeanInfo implements Comparable<BeanInfo> {
        
        private int level = 0;  //继承关系：如果是子类，那么level比父类大，排在后面
        private Object bean;
        
        private Map<String, StandardMethodMetadata> thisMethods = new HashMap<>();
        private Map<String, Method> superMethods = new HashMap<>();
        
        public BeanInfo(Class clazz){

            this.bean = CatServerUtil.getBeanOfType(clazz);
            Class thisClazz = bean.getClass();
            
            
            Class superClass = thisClazz;
            List<Class> inters = new ArrayList<>();
            
            while ( superClass != Object.class ) {
                for ( Class inter : superClass.getInterfaces() ) {
                    inters.add(inter);
                }
                superClass = superClass.getSuperclass();
                level = level + 1;
            }

            for( Class inter : thisClazz.getInterfaces() ){
                if( inter.getSimpleName().contains(bridgeName) ){
                    Method[] methods = inter.getMethods();
                    for(Method method : methods){
                        superMethods.put(method.getName(), method);
                    }
                }
            }
            
            for ( Class inter : inters ){
                Method[] methods = inter.getMethods();
                for(Method method : methods){
                    StandardMethodMetadata metadata = new StandardMethodMetadata(method);
                    Map<String, Object> attr = metadata.getAnnotationAttributes(annName);
                    if( attr != null){
                        thisMethods.put(CatToosUtil.signature(method), metadata);
                    }
                }
            }
        }
        
        
        @Override
        public int compareTo(BeanInfo info) {
            return level - info.level;
        }
    }


    private final <T> T[] getValue(Map<String, Object> map, String key, IntFunction<T[]> func){
        Object value = map.get(key);
        if( value instanceof List ){
            List<T> list = ((List<T>)value);
            return list.toArray(func.apply(list.size()));
        } else if(value.getClass().isArray()){
            return (T[]) value;
        } else {
            T[] arr = func.apply(1);
            arr[0] = (T) value;
            return arr;
        }
    }

    
    
    public List<Class> getCatServerList() {
        return catServerList;
    }
    public void setCatServerList(List<Class> catServerList) {
        this.catServerList = catServerList;
    }

    public BeanDefinitionRegistry getRegistry() {
        return registry;
    }
    public void setRegistry(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }
}
