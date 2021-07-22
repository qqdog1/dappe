package name.qd.dappe.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ResponseEntityExceptionHandler {
	@ExceptionHandler(value = Exception.class)
	protected ResponseEntity<String> handleException(Exception e) {
		return ResponseEntity.badRequest().body(e.toString());
	}
}
