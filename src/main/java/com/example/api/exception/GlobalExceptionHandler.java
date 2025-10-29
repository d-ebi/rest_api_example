package com.example.api.exception;

import com.example.api.config.TraceIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 例外の集約ハンドラ。
 * 仕様に基づくHTTPステータスと共通エラーレスポンスを返却します。
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private String traceId() {
        String id = MDC.get(TraceIdFilter.TRACE_ID_KEY);
        return id != null ? id : "";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage(), ex);
        List<FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new FieldErrorDetail(
                        "VALIDATION_ERROR",
                        err.getDefaultMessage(),
                        err.getField(),
                        null))
                .collect(Collectors.toList());
        ApiErrorResponse body = new ApiErrorResponse("BAD_REQUEST", "Validation failed", traceId(), details);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        log.error("Constraint violation: {}", ex.getMessage(), ex);
        List<FieldErrorDetail> details = ex.getConstraintViolations().stream()
                .map(v -> new FieldErrorDetail(
                        "VALIDATION_ERROR",
                        v.getMessage(),
                        v.getPropertyPath() != null ? v.getPropertyPath().toString() : null,
                        null))
                .collect(Collectors.toList());
        ApiErrorResponse body = new ApiErrorResponse("BAD_REQUEST", "Validation failed", traceId(), details);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage(), ex);
        ApiErrorResponse body = new ApiErrorResponse("NOT_FOUND", ex.getMessage(), traceId());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException ex) {
        log.error("Conflict: {}", ex.getMessage(), ex);
        ApiErrorResponse body = new ApiErrorResponse("CONFLICT", ex.getMessage(), traceId(), ex.getErrors());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ApiErrorResponse> handleUnprocessable(UnprocessableEntityException ex) {
        log.error("Unprocessable entity: {}", ex.getMessage(), ex);
        ApiErrorResponse body = new ApiErrorResponse("UNPROCESSABLE_ENTITY", ex.getMessage(), traceId(), ex.getErrors());
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        log.error("Method not allowed: {}", ex.getMessage(), ex);
        ApiErrorResponse body = new ApiErrorResponse("METHOD_NOT_ALLOWED", ex.getMessage(), traceId());
        return new ResponseEntity<>(body, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        log.error("Not acceptable: {}", ex.getMessage(), ex);
        ApiErrorResponse body = new ApiErrorResponse("NOT_ACCEPTABLE", ex.getMessage(), traceId());
        return new ResponseEntity<>(body, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("Type mismatch: {}", ex.getMessage(), ex);
        ApiErrorResponse body = new ApiErrorResponse("BAD_REQUEST", ex.getMessage(), traceId());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        log.error("Message not readable: {}", ex.getMessage(), ex);
        ApiErrorResponse body = new ApiErrorResponse("BAD_REQUEST", "Malformed JSON or invalid format", traceId());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandler(NoHandlerFoundException ex) {
        log.error("No handler found: {}", ex.getMessage(), ex);
        ApiErrorResponse body = new ApiErrorResponse("NOT_FOUND", ex.getMessage(), traceId());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException ex) {
        log.error("Bad request: {}", ex.getMessage(), ex);
        ApiErrorResponse body = new ApiErrorResponse("BAD_REQUEST", ex.getMessage(), traceId(), ex.getErrors());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleOther(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ApiErrorResponse body = new ApiErrorResponse("INTERNAL_SERVER_ERROR", "Unexpected error", traceId());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
