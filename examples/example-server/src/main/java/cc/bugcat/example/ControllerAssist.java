package cc.bugcat.example;

import cc.bugcat.catface.utils.CatToosUtil;
import cc.bugcat.catserver.asm.CatServerInstance;
import cc.bugcat.catserver.handler.CatControllerAssist;
import cc.bugcat.example.tools.ResponseEntity;
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
import java.util.List;

/**
 * 异常响应处理。将异常包装为固定的格式并返回。
 * */
@ControllerAdvice
public class ControllerAssist {
	
	/**
	 * 处理服务器端Exception
	 */
	@ExceptionHandler({ Throwable.class })
	@ResponseBody
	public Object handleException(Throwable throwable, HandlerMethod handlerMethod, HttpServletRequest request, HttpServletResponse response) {
		Object result = null;	
		try {
			throwable = CatToosUtil.getCause(throwable);

			if( handlerMethod.getBean() instanceof CatServerInstance ){
				return CatControllerAssist.doExceptionHandler(throwable, handlerMethod);
			}
			
			String exc = throwable.getClass().getSimpleName().toUpperCase();
			if ( throwable instanceof MethodArgumentNotValidException || throwable instanceof BindingResult || throwable instanceof BindException) {  //参数异常
				BindingResult bindingResult = null;
				if ( throwable instanceof MethodArgumentNotValidException ) { // mvc RequestBody 参数异常
					MethodArgumentNotValidException manv = (MethodArgumentNotValidException) throwable;
					bindingResult = manv.getBindingResult();
				} else if ( throwable instanceof BindException ) {        // mvc RequestParam 参数异常
					BindException bde = (BindException) throwable;
					bindingResult = bde.getBindingResult();
				} else if ( throwable instanceof BindingResult) {
					bindingResult = (BindingResult) throwable;
				}
				
				String message = "请求参数异常";
				if ( bindingResult != null && bindingResult.hasErrors() ) {
					List<ObjectError> objectErrorList = bindingResult.getAllErrors();
					if ( objectErrorList != null && objectErrorList.size() > 0 ) {
						ObjectError objectError = objectErrorList.get(0);
						Object[] regs = objectError.getArguments();
						DefaultMessageSourceResolvable dmsr = (DefaultMessageSourceResolvable) regs[0];
						message = "[" + dmsr.getCodes()[0] + "]" + objectError.getDefaultMessage();
					}
				}
				result = ResponseEntity.fail("500", message);
			} else if ( throwable instanceof JsonProcessingException ) { // json格式异常
				String message = ((JsonProcessingException) throwable).getOriginalMessage();
				result = ResponseEntity.fail("500", "[JsonProcessingException]" + message);
			} else if ( "CLIENTABORTEXCEPTION".equals(exc) ) {
				return null;
			} else if ( "SQLEXCEPTION".equals(exc) || "DUPLICATEKEYEXCEPTION".equals(exc) ) {//sql异常

			} else if ( "TIMEOUTEXCEPTION".equals(exc) ) {//链接超时

			} else if ( throwable instanceof OutOfMemoryError ) {

			} else {
				// 默认
			}
			if( result == null ){
				result = ResponseEntity.fail("500", throwable.getMessage());
			}
			return result;
			
		} catch ( Throwable es ) {
			return ResponseEntity.fail("500", throwable.getMessage());
		}
	}

}
