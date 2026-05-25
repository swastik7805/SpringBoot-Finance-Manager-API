package com.example.syfe.exceptions;

import com.example.syfe.dtos.responses.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j // For generating logger
@RestControllerAdvice // Handles exceptions of all controllers
public class GlobalExceptionHandler {

    // 400 - @Valid errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex,HttpServletRequest request) {

        List<ApiErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> ApiErrorResponse.FieldError.builder()
                        .field(fe.getField())
                        .message(fe.getDefaultMessage())
                        .rejectedValue(fe.getRejectedValue())
                        .build())
                .toList();

        ApiErrorResponse response = ApiErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("One or more fields have validation errors")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // 400 - Business Rule Violations
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRuleException(BusinessRuleException ex,HttpServletRequest request) {

        ApiErrorResponse response = buildErrorResponse(HttpStatus.BAD_REQUEST,ex.getMessage(),request);

        return ResponseEntity.badRequest().body(response);
    }

    // 400 - Unreadable Request Body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex,HttpServletRequest request) {

        ApiErrorResponse response = buildErrorResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request body", request);

        return ResponseEntity.badRequest().body(response);
    }

    // 400 - Type Mismatch (string instead of number)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,HttpServletRequest request) {

        String message = String.format("Parameter '%s' should be of type '%s'",ex.getName(),ex.getRequiredType()!=null ? ex.getRequiredType().getSimpleName() : "unknown");

        ApiErrorResponse response = buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);

        return ResponseEntity.badRequest().body(response);
    }

    // 404 - Resource Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,HttpServletRequest request) {
        ApiErrorResponse response = buildErrorResponse(HttpStatus.NOT_FOUND,ex.getMessage(),request);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // 404 - No Static Resource Found
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(NoResourceFoundException ex,HttpServletRequest request) {
        ApiErrorResponse response = buildErrorResponse(HttpStatus.NOT_FOUND, "The requested endpoint does not exist", request);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    // 405 - Method Not Allowed
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,HttpServletRequest request) {
        String message = String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod());
        ApiErrorResponse response = buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, message, request);
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }
    
    // 401 - Bad Credentials (login failure)    
    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(AuthenticationException ex,HttpServletRequest request) {
        ApiErrorResponse response = buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password", request);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    
    // 409 - Duplicate Resource
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateResource(DuplicateResourceException ex,HttpServletRequest request) {
        ApiErrorResponse response = buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // 500 - Catch-All
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllUncaughtExceptions(Exception ex,HttpServletRequest request) {
        log.error("Unhandled exception at {}",request.getRequestURI(),ex);
        ApiErrorResponse response = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,"An unexpected error occurred. Please try again later.",request);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Helper
    private ApiErrorResponse buildErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        return ApiErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
