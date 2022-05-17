package cc.bugcat.catserver.handler;

import cc.bugcat.catserver.asm.CatServerHandler;
import cc.bugcat.catserver.spi.CatResultHandler;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Factory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 异常响应处理，将异常包装为固定的格式并返回。
 * 
 * 可以使用<b>cat-server.controller-assist.enable=false</b>关闭
 * */
@Order
@Conditional(CatControllerAssist.Enable.class)
@ControllerAdvice(assignableTypes = CatServerHandler.class)
public class CatControllerAssist {
	
	public static class Enable implements Condition {
		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata annotatedTypeMetadata) {
			return "true".equals(context.getEnvironment().resolvePlaceholders("${cat-server.controller-assist.enable:true}"));
		}
	}
	
	
	@ExceptionHandler({ Throwable.class })
	@ResponseBody
	public Object handleException(Throwable throwable, HandlerMethod handlerMethod, HttpServletRequest request, HttpServletResponse response) {
		try {
			
			while ( throwable.getCause() != null ) {
				throwable = throwable.getCause();
			}

			Method method = handlerMethod.getMethod();

			Factory ctrlBean = (Factory) handlerMethod.getBean();

			CatMethodAopInterceptor methodInterceptor = null;
			Callback[] callbacks = ctrlBean.getCallbacks();
			for ( int idx = 0; idx < callbacks.length; idx ++ ){
				if( callbacks[idx] instanceof CatMethodAopInterceptor){
					CatMethodAopInterceptor interceptor = (CatMethodAopInterceptor) callbacks[idx];
					if( interceptor.equalsMethod(method) ){
						methodInterceptor = interceptor;
						break;
					}
				}
			}

			CatMethodInfo methodInfo = methodInterceptor.getMethodInfo();
			CatResultHandler resultHandler = methodInfo.getResultHandler();


			Object result = resultHandler.onError(throwable, handlerMethod.getMethod().getReturnType());
			return result;
		} catch ( Throwable es ) {
			throw new RuntimeException(es);
		}
	}
}
