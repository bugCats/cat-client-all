package cc.bugcat.catclient.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


/**
 * spring 容器
 * @author bugcat
 * */
public class CatClientUtil implements ApplicationContextAware {


    public static final Pattern PARAM_KEY_PAT = Pattern.compile("^\\#\\{(.+)\\}$");
    
    /**
     * 自定义组件容器
     * 非spring容器时使用
     * */
    private static Map<Class, Object> catClinetMap = new ConcurrentHashMap<>();
    
    /**
     * spring容器
     * */
    private static ApplicationContext context;


    
    @Override
    public synchronized void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if ( context == null ) {
            context = applicationContext;
        }
    }



    /**
     * 优先从Spring容器中获取
     * 其次catClinetMap
     * 都没有则返回null
     * */
    public static <T> T getBean(Class<T> clazz){
        try {
            return context.getBean(clazz);
        } catch ( Exception ex ) {
            try {
                Map<String, T> beans = context.getBeansOfType(clazz);
                if( beans.size() == 1 ){
                    return beans.values().iterator().next();
                } else {
                    for(T value : beans.values()){
                        if( clazz == value.getClass()){
                            return value;
                        }
                        String clazzName = value.getClass().getSimpleName();
                        int start = clazzName.indexOf("$$");
                        if( start > -1 ) {
                            clazzName = clazzName.substring(0, start);
                        }
                        if( clazz.getSimpleName().equals(clazzName) ){
                            return value;
                        }
                    }
                }
            } catch ( Exception e ) {

            }
            return (T) catClinetMap.get(clazz);
        }
    }

    
    /**
     * catClinetMap注册bean
     * */
    public void registerBean(Object bean){
        catClinetMap.putIfAbsent(bean.getClass(), bean);
    }

    protected static void registerBean(Class type, Object bean){
        catClinetMap.putIfAbsent(type, bean);
    }

    /**
     * catClinetMap是否包含
     * */
    public static boolean contains(Class key) {
        return catClinetMap.containsKey(key);
    }


    public static ApplicationContext getContext(){
        return context;
    }
    public static Environment getEnvironment(){
        return context != null ? context.getEnvironment() : null;
    }

}