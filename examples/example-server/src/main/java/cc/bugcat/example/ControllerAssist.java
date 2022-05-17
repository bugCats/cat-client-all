package cc.bugcat.example;

import cc.bugcat.catserver.annotation.CatServer;
import cc.bugcat.catserver.asm.CatServerHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
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
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * 异常响应处理。将异常包装为固定的格式并返回。
 * */
public class ControllerAssist {
	
	/**
	 * 处理服务器端Exception
	 */
	@ExceptionHandler({ Throwable.class })
	@ResponseBody
	public Object handleException(Throwable throwable, HandlerMethod handlerMethod, HttpServletRequest request, HttpServletResponse response) {
		Object result = null;	
		try {
			while ( throwable.getCause() != null ) {
				throwable = throwable.getCause();
			}

			CatServerHandler catServerHandler = (CatServerHandler)handlerMethod.getBean();
			Class serverClass = catServerHandler.getCatServerClass();
			Annotation[] annotations = serverClass.getAnnotations();

			try {
				String exc = throwable.getClass().getSimpleName().toUpperCase();
				if ( "WEBEXCHANGEBINDEXCEPTION".equals(exc) || "METHODARGUMENTNOTVALIDEXCEPTION".equals(exc) || "BINDEXCEPTION".equals(exc) || //参数异常
						throwable instanceof JsonProcessingException ) { // json格式异常

					String tips = "9998";
					String message = "请求参数异常";
					Class<?> onErrObject = handlerMethod.getBeanType();

					BindingResult bindingResult = null;
					if ( throwable instanceof MethodArgumentNotValidException ) { // mvc RequestBody 参数异常
						MethodArgumentNotValidException manv = (MethodArgumentNotValidException) throwable;
						bindingResult = manv.getBindingResult();
					} else if ( throwable instanceof BindException ) {        // mvc RequestParam 参数异常
						BindException bde = (BindException) throwable;
						bindingResult = bde.getBindingResult();
					} else if ( throwable instanceof JsonProcessingException ) {
						message = ((JsonProcessingException) throwable).getOriginalMessage();
					}

					if ( bindingResult != null && bindingResult.hasErrors() ) {
						List<ObjectError> objectErrorList = bindingResult.getAllErrors();
						if ( objectErrorList != null && objectErrorList.size() > 0 ) {
							ObjectError objectError = objectErrorList.get(0);
							Object[] regs = objectError.getArguments();
							DefaultMessageSourceResolvable dmsr = (DefaultMessageSourceResolvable) regs[0];

							tips = objectErrorList.get(0).getDefaultMessage();
							message = "[ " + dmsr.getCodes()[0] + " ] " + tips;
						}
					}
					
					
				} else if ( "CLIENTABORTEXCEPTION".equals(exc) ) {
					return null;
				} else if ( "SQLEXCEPTION".equals(exc) || "DUPLICATEKEYEXCEPTION".equals(exc) ) {//sql异常
					
				} else if ( "TIMEOUTEXCEPTION".equals(exc) ) {//链接超时
					
				} else if ( throwable instanceof OutOfMemoryError ) {
					
				} else {
					// 默认
				}
			} catch ( Exception es ) {
				
			}
			
			return result;
		} catch ( Exception es ) {
			return result;
		}

	}

	
	

}
