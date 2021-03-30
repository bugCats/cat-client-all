package com.bugcat.catserver.scanner;

import com.bugcat.catface.utils.CatToosUtil;
import com.bugcat.catserver.asm.CatAsm;
import com.bugcat.catserver.utils.CatServerUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.StandardMethodMetadata;
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
 * 将动态生成的interface实现类，注册成Controller
 * @author: bugcat
 * */
public class CatServerInitBean implements InitializingBean{


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
        
        /**
         * level越大，说明继承次数越多，
         * 优先解析level小的对象，这样level大的对象，会覆盖level小的对象，保证继承性
         * */
        beanInfos.sort(BeanInfo::compareTo);
        
        IntFunction<RequestMethod[]> requestMethodToArray = RequestMethod[]::new;
        IntFunction<String[]> stringToArray = String[]::new;

        RequestMappingHandlerMapping mapper = CatServerUtil.getBean(RequestMappingHandlerMapping.class);
        for( BeanInfo info : beanInfos ){
            for(Map.Entry<String, Method> entry : info.bridgeMethods.entrySet() ){
                Method method = entry.getValue();
                if( method != null ){// RequestMapping必须指向桥接方法
                    StandardMethodMetadata metadata = new StandardMethodMetadata(method);
                    Map<String, Object> attr = metadata.getAnnotationAttributes(CatServerUtil.annName);
                    RequestMappingInfo mappingInfo = RequestMappingInfo
                            .paths(getValue(attr, "value", stringToArray))
                            .methods(getValue(attr, "method", requestMethodToArray))
                            .params(getValue(attr, "params", stringToArray))
                            .headers(getValue(attr, "headers", stringToArray))
                            .produces(getValue(attr, "produces", stringToArray))
                            .consumes(getValue(attr, "consumes", stringToArray))
                            .build();
                    mapper.unregisterMapping(mappingInfo);
                    mapper.registerMapping(mappingInfo, info.ctrl, method); // 注册映射处理
                }  
            }
        }
    }

    
    
    private static class BeanInfo implements Comparable<BeanInfo> {
        
        private int level = 0;  //继承关系：如果是子类，那么level比父类大，排在后面
        private Object ctrl;
        private Map<String, Method> bridgeMethods = new HashMap<>();
        
        public BeanInfo(Class clazz){
            this.ctrl = CatServerUtil.getCtrlClass(clazz);
            Class thisClazz = ctrl.getClass(); //cglib动态生成的class => interface的实现类
            List<Class> inters = new ArrayList<>();
            Class superClass = thisClazz;
            while ( superClass != Object.class ) {
                for ( Class inter : superClass.getInterfaces() ) {
                    inters.add(inter);
                }
                superClass = superClass.getSuperclass();
                level = level + 1;
            }

            for( Class inter : inters ){ //增强后的interface
                if ( CatAsm.isBridgeClass(inter) ) {
                    for( Method method : inter.getMethods() ){
                        bridgeMethods.put(CatToosUtil.signature(method), method);
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
