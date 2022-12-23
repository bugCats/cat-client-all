package cc.bugcat.example.tools;

import cc.bugcat.catclient.handler.CatClientLogger;
import cc.bugcat.catclient.handler.CatSendContextHolder;
import cc.bugcat.catface.spi.AbstractResponesWrapper;
import cc.bugcat.catface.spi.CatTypeReference;

import java.lang.reflect.Type;

/**
 * http响应包装器类处理
 *
 * @see AbstractResponesWrapper
 * @author bugcat
 * */
public class ResponseEntityWrapper extends AbstractResponesWrapper<ResponseEntity>{


    @Override
    public Class<ResponseEntity> getWrapperClass() {
        return ResponseEntity.class;
    }

    /**
     * 获取json转对象泛型
     */
    @Override
    public <T> CatTypeReference getWrapperType(Type type){
        return new CatTypeReference<ResponseEntity<T>>(type){};
    }

    /**
     * 校验业务
     * 直接抛出异常
     */
    @Override
    public void checkValid(ResponseEntity wrapper) {
        if( ResponseEntity.succ.equals(wrapper.getErrCode())){
            //正常
        } else {
            CatSendContextHolder contextHolder = CatSendContextHolder.getContextHolder();
            CatClientLogger lastCatLog = contextHolder.getSendHandler().getHttpPoint().getLastCatLog();
            lastCatLog.setErrorMessge("[" + wrapper.getErrCode() + "]" + wrapper.getErrMsg());

            //业务异常，可以直接继续抛出，在公共的异常处理类中，统一处理
            throw new RuntimeException(lastCatLog.getErrorMessge());
        }
    }


    @Override
    public Object getValue(ResponseEntity wrapper) {
        return wrapper.getData();
    }


    @Override
    public ResponseEntity createEntryOnSuccess(Object value, Type returnType) {
        return ResponseEntity.ok(value);
    }

    @Override
    public ResponseEntity createEntryOnException(Throwable throwable, Type returnType) {
        throwable.printStackTrace();
        return ResponseEntity.fail("-1", throwable.getMessage() == null ? "NullPointerException" : throwable.getMessage());
    }

}
