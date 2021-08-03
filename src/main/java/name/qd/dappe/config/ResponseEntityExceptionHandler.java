package name.qd.dappe.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ResponseEntityExceptionHandler {
	private static Logger logger = LoggerFactory.getLogger(ResponseEntityExceptionHandler.class);
	
	@ExceptionHandler(value = Exception.class)
	protected ResponseEntity<String> handleException(Exception e) {
		logger.error(e.getMessage(), e);
		return ResponseEntity.badRequest().body(e.toString());
	}
}
