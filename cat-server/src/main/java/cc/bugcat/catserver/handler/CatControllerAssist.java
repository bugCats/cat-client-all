package cc.bugcat.catserver.handler;

import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.asm.CatServerInstance;
import cc.bugcat.catserver.beanInfos.CatServerInfo;
import cc.bugcat.catserver.spi.CatResultHandler;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 异常响应处理，将异常包装为固定的格式并返回。
 * 这里的异常，入参验证、controllor切面造成的。CatServer类异常，直接会被{@link CatResultHandler#onError(java.lang.Throwable, java.lang.Class)}处理
 * 可以使用<b>cat-server.controller-assist.enable=false</b>关闭
 * */
@Order
@Conditional(CatControllerAssist.Enable.class)
@ControllerAdvice(assignableTypes = CatServerInstance.class)
public class CatControllerAssist {
	
	
	public static class Enable implements Condition {
		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata annotatedTypeMetadata) {
			return "true".equalsIgnoreCase(context.getEnvironment().resolvePlaceholders("${cat-server.controller-assist.enable:true}"));
		}
	}
	
	
	
	@ExceptionHandler({ Throwable.class })
	@ResponseBody
	public Object handleException(Throwable throwable, HandlerMethod handlerMethod, HttpServletRequest request, HttpServletResponse response) throws Throwable {
		Object resp = doExceptionHandler(throwable, handlerMethod);
		return resp;
	}
	
	
	public static Object doExceptionHandler(Throwable throwable, HandlerMethod handlerMethod) throws Throwable {
		Throwable exc = CatToosUtil.getCause(throwable);
		String message = null;
		if ( exc instanceof MethodArgumentNotValidException || exc instanceof BindingResult || exc instanceof BindException ) {  //参数异常
			BindingResult bindingResult = null;
			if ( exc instanceof MethodArgumentNotValidException ) { // mvc RequestBody 参数异常
				MethodArgumentNotValidException manv = (MethodArgumentNotValidException) exc;
				bindingResult = manv.getBindingResult();
			} else if ( exc instanceof BindException ) {        // mvc RequestParam 参数异常
				BindException bde = (BindException) exc;
				bindingResult = bde.getBindingResult();
			} else if ( exc instanceof BindingResult) {
				bindingResult = (BindingResult) exc;
			}
			if ( bindingResult != null && bindingResult.hasErrors() ) {
				List<ObjectError> objectErrorList = bindingResult.getAllErrors();
				if ( objectErrorList != null && objectErrorList.size() > 0 ) {
					ObjectError objectError = objectErrorList.get(0);
					Object[] regs = objectError.getArguments();
					DefaultMessageSourceResolvable dmsr = (DefaultMessageSourceResolvable) regs[0];
					message = "[" + dmsr.getCodes()[0] + "]" + objectError.getDefaultMessage();
				}
			}
		}
		if( CatToosUtil.isNotBlank(message) ){
			Throwable argsExc = new Exception(message);
			argsExc.setStackTrace(exc.getStackTrace());
			exc = argsExc;
		}
		CatServerInstance catServerInstance = (CatServerInstance)handlerMethod.getBean();
		CatServerInfo serverInfo = catServerInstance.getServerProperty().getServerInfo();
		CatResultHandler resultHandler = serverInfo.getResultHandler();
		Object result = resultHandler.onError(exc, handlerMethod.getMethod().getReturnType());
		return result;
	}
	
	
}
