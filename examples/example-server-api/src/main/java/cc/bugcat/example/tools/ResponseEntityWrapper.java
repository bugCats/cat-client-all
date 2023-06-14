package cc.bugcat.example.tools;

import cc.bugcat.catclient.handler.CatClientContextHolder;
import cc.bugcat.catclient.handler.CatClientLogger;
import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catface.spi.CatTypeReference;

import java.lang.reflect.Type;

/**
 * http响应包装器类处理。
 * 如果在客户端，则为拆包装器；
 * 如果在服务端，则为加包装器；
 *
 * @see AbstractResponesWrapper
 * @author bugcat
 * */
public class ResponseEntityWrapper extends AbstractResponesWrapper<ResponseEntity>{

    /**
     * 返回包装器class
     * */
    @Override
    public Class<ResponseEntity> getWrapperClass() {
        return ResponseEntity.class;
    }

    /**
     * 组装包装器类中的实际泛型
     * */
    @Override
    public <T> CatTypeReference getWrapperType(Type type){
        return new CatTypeReference<ResponseEntity<T>>(type){};
    }

    /**
     * 拆包装器，并且自动校验业务是否成功？
     * 本示例直接继续抛出异常
     * */
    @Override
    public void checkValid(ResponseEntity wrapper) throws Exception {
        if( ResponseEntity.succ.equals(wrapper.getErrCode())){
            //正常
        } else {
            
            //记录日志
            CatClientContextHolder contextHolder = CatClientContextHolder.getContextHolder();
            CatClientLogger lastCatLog = contextHolder.getSendHandler().getHttpPoint().getLastCatLog();
            lastCatLog.setErrorMessge("[" + wrapper.getErrCode() + "]" + wrapper.getErrMsg());

            //业务异常，可以直接继续抛出，在公共的异常处理类中，统一处理
            throw new RuntimeException(lastCatLog.getErrorMessge());
        }
    }

    /**
     * 拆包装器，获取包装器类中的业务对象
     * */
    @Override
    public Object getValue(ResponseEntity wrapper) {
        return wrapper.getData();
    }


    /**
     * 加包装器类
     * */
    @Override
    public ResponseEntity createEntryOnSuccess(Object value, Class methodReturnClass) {
        return ResponseEntity.ok(value);
    }

    /**
     * 当发生异常时加包装器
     * */
    @Override
    public ResponseEntity createEntryOnException(Throwable throwable, Class methodReturnClass) {
        throwable.printStackTrace();
        return ResponseEntity.fail("-1", throwable.getMessage() == null ? "NullPointerException" : throwable.getMessage());
    }

}
