package com.portfolio.manager.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

		@ExceptionHandler(ResourceNotFoundException.class)
		public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
				log.warn("Resource not found: {}", ex.getMessage());
				return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
		}

		@ExceptionHandler(BusinessException.class)
		public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, WebRequest request) {
				log.warn("Business rule violation: {}", ex.getMessage());
				return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
		}

		@ExceptionHandler(InvalidStatusTransitionException.class)
		public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidStatusTransitionException ex, WebRequest request) {
				log.warn("Invalid status transition: {}", ex.getMessage());
				return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
		}

		@ExceptionHandler(MethodArgumentNotValidException.class)
		public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
				Map<String, String> fieldErrors = new HashMap<>();
				for (FieldError error : ex.getBindingResult().getFieldErrors()) {
						fieldErrors.put(error.getField(), error.getDefaultMessage());
				}
				ErrorResponse response = ErrorResponse.builder()
						.timestamp(LocalDateTime.now())
						.status(HttpStatus.BAD_REQUEST.value())
						.error("Erro de validação")
						.message("Um ou mais campos são inválidos")
						.path(request.getDescription(false).replace("uri=", ""))
						.fieldErrors(fieldErrors)
						.build();
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		@ExceptionHandler(MemberServiceException.class)
		public ResponseEntity<ErrorResponse> handleMemberService(MemberServiceException ex, WebRequest request) {
				log.error("Member service error: {}", ex.getMessage());
				return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request);
		}

		@ExceptionHandler(Exception.class)
		public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, WebRequest request) {
				log.error("Unexpected error: {}", ex.getMessage(), ex);
				return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor", request);
		}

		private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, WebRequest request) {
				ErrorResponse response = ErrorResponse.builder()
						.timestamp(LocalDateTime.now())
						.status(status.value())
						.error(status.getReasonPhrase())
						.message(message)
						.path(request.getDescription(false).replace("uri=", ""))
						.build();
				return ResponseEntity.status(status).body(response);
		}
}
