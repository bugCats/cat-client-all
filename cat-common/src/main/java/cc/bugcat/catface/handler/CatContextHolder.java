package cc.bugcat.catface.handler;


/**
 * CatClient、CatServer发送异常时，存储上下文异常对象
 * */
public class CatContextHolder {


    private static ThreadLocal<ContextEntry> threadLocal = new ThreadLocal<>();


    public static Throwable currentException() {
        ContextEntry contextEntry = threadLocal.get();
        return contextEntry == null ? null : contextEntry.throwable;
    }
    public static <T> T currentContext(Class<T> contextClass) {
        ContextEntry contextEntry = threadLocal.get();
        return contextEntry == null ? null : (T) contextEntry.context;
    }
    
    
    public static void setContext(Object context){
        threadLocal.set(new ContextEntry(context));
    }
    public static void setException(Throwable throwable){
        ContextEntry contextEntry = threadLocal.get();
        if( contextEntry != null ){
            contextEntry.throwable = throwable;
        }
    }
    public static void remove(){
        threadLocal.remove();
    }
    
    
    
    private static class ContextEntry {
        private Object context;
        private Throwable throwable;

        private ContextEntry(Object context) {
            this.context = context;
        }
    }
    
}
