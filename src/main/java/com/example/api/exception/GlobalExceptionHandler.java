package com.example.api.exception;

import com.example.api.config.TraceIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

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

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String message) {
        return response(status, message, null);
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String message, List<FieldErrorDetail> errors) {
        ApiErrorResponse body = ErrorCatalog.toResponse(status, message, traceId(), errors);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage(), ex);
        List<FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> ErrorCatalog.fieldError(
                        ErrorCatalog.DetailCodes.VALIDATION_ERROR,
                        err.getDefaultMessage(),
                        err.getField(),
                        null))
                .collect(Collectors.toList());
        return response(HttpStatus.BAD_REQUEST, ErrorCatalog.Messages.VALIDATION_FAILED, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        log.error("Constraint violation: {}", ex.getMessage(), ex);
        List<FieldErrorDetail> details = ex.getConstraintViolations().stream()
                .map(v -> ErrorCatalog.fieldError(
                        ErrorCatalog.DetailCodes.VALIDATION_ERROR,
                        v.getMessage(),
                        v.getPropertyPath() != null ? v.getPropertyPath().toString() : null,
                        null))
                .collect(Collectors.toList());
        return response(HttpStatus.BAD_REQUEST, ErrorCatalog.Messages.VALIDATION_FAILED, details);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage(), ex);
        return response(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException ex) {
        log.error("Conflict: {}", ex.getMessage(), ex);
        return response(HttpStatus.CONFLICT, ex.getMessage(), ex.getErrors());
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ApiErrorResponse> handleUnprocessable(UnprocessableEntityException ex) {
        log.error("Unprocessable entity: {}", ex.getMessage(), ex);
        return response(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), ex.getErrors());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        log.error("Method not allowed: {}", ex.getMessage(), ex);
        return response(HttpStatus.METHOD_NOT_ALLOWED, ErrorCatalog.Messages.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        log.error("Not acceptable: {}", ex.getMessage(), ex);
        return response(HttpStatus.NOT_ACCEPTABLE, ErrorCatalog.Messages.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("Type mismatch: {}", ex.getMessage(), ex);
        return response(HttpStatus.BAD_REQUEST, ErrorCatalog.Messages.TYPE_MISMATCH);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        log.error("Message not readable: {}", ex.getMessage(), ex);
        return response(HttpStatus.BAD_REQUEST, ErrorCatalog.Messages.MALFORMED_JSON);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandler(NoHandlerFoundException ex) {
        log.error("No handler found: {}", ex.getMessage(), ex);
        return response(HttpStatus.NOT_FOUND, ErrorCatalog.Messages.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException ex) {
        log.error("Bad request: {}", ex.getMessage(), ex);
        return response(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getErrors());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        log.error("Response status exception: {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = StringUtils.hasText(ex.getReason()) ? ex.getReason() : null;
        return response(status, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleOther(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        HttpStatus status = resolveStatusFromAnnotation(ex);
        String message = resolveMessageFromAnnotation(ex, status);
        return response(status, message);
    }

    private HttpStatus resolveStatusFromAnnotation(Exception ex) {
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            HttpStatus status = responseStatus.code();
            if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
                status = responseStatus.value();
            }
            if (status != HttpStatus.INTERNAL_SERVER_ERROR) {
                return status;
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessageFromAnnotation(Exception ex, HttpStatus status) {
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
        if (responseStatus != null && StringUtils.hasText(responseStatus.reason())) {
            return responseStatus.reason();
        }
        if (StringUtils.hasText(ex.getMessage())) {
            return ex.getMessage();
        }
        return status == HttpStatus.INTERNAL_SERVER_ERROR ? ErrorCatalog.Messages.UNEXPECTED_ERROR : null;
    }
}
