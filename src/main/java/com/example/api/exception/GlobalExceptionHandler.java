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

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
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
        logClientEvent("Validation error", ex);
        List<FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> ErrorCatalog.fieldError(
                        ErrorCatalog.DetailCodes.VALIDATION_ERROR,
                        err.getDefaultMessage(),
                        err.getField(),
                        null))
                .collect(Collectors.toList());
        HttpStatus status = resolveStatusForFieldErrors(ex.getBindingResult().getFieldErrors());
        return response(status, ErrorCatalog.Messages.VALIDATION_FAILED, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        logClientEvent("Constraint violation", ex);
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        List<FieldErrorDetail> details = violations.stream()
                .map(v -> ErrorCatalog.fieldError(
                        ErrorCatalog.DetailCodes.VALIDATION_ERROR,
                        v.getMessage(),
                        v.getPropertyPath() != null ? v.getPropertyPath().toString() : null,
                        null))
                .collect(Collectors.toList());
        HttpStatus status = resolveStatusForViolations(violations);
        return response(status, ErrorCatalog.Messages.VALIDATION_FAILED, details);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        logClientEvent("Resource not found", ex);
        return response(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException ex) {
        logClientEvent("Conflict", ex);
        return response(HttpStatus.CONFLICT, ex.getMessage(), ex.getErrors());
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ApiErrorResponse> handleUnprocessable(UnprocessableEntityException ex) {
        logClientEvent("Unprocessable entity", ex);
        return response(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), ex.getErrors());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        logClientEvent("Method not allowed", ex);
        return response(HttpStatus.METHOD_NOT_ALLOWED, ErrorCatalog.Messages.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        logClientEvent("Not acceptable", ex);
        return response(HttpStatus.NOT_ACCEPTABLE, ErrorCatalog.Messages.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        logClientEvent("Type mismatch", ex);
        return response(HttpStatus.BAD_REQUEST, ErrorCatalog.Messages.TYPE_MISMATCH);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        logClientEvent("Message not readable", ex);
        return response(HttpStatus.BAD_REQUEST, ErrorCatalog.Messages.MALFORMED_JSON);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandler(NoHandlerFoundException ex) {
        logClientEvent("No handler found", ex);
        return response(HttpStatus.NOT_FOUND, ErrorCatalog.Messages.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException ex) {
        logClientEvent("Bad request", ex);
        return response(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getErrors());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        logByStatus("Response status exception", ex, status);
        String message = StringUtils.hasText(ex.getReason()) ? ex.getReason() : null;
        return response(status, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleOther(Exception ex) {
        HttpStatus status = resolveStatusFromAnnotation(ex);
        logByStatus("Unexpected error", ex, status);
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

    private HttpStatus resolveStatusForFieldErrors(List<org.springframework.validation.FieldError> errors) {
        boolean badRequest = errors.stream().anyMatch(this::isBadRequestViolation);
        return badRequest ? HttpStatus.BAD_REQUEST : HttpStatus.UNPROCESSABLE_ENTITY;
    }

    private boolean isBadRequestViolation(org.springframework.validation.FieldError error) {
        if (error.isBindingFailure()) {
            return true;
        }
        String[] codes = error.getCodes();
        if (codes == null) return false;
        for (String code : codes) {
            if (code == null) continue;
            String simple = simpleCode(code);
            if ("NotNull".equals(simple)) {
                return true;
            }
        }
        return false;
    }

    private HttpStatus resolveStatusForViolations(Set<ConstraintViolation<?>> violations) {
        boolean badRequest = violations.stream().anyMatch(this::isBadRequestViolation);
        return badRequest ? HttpStatus.BAD_REQUEST : HttpStatus.UNPROCESSABLE_ENTITY;
    }

    private boolean isBadRequestViolation(ConstraintViolation<?> violation) {
        String annotationName = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
        return "NotNull".equals(annotationName);
    }

    private String simpleCode(String code) {
        int idx = code.lastIndexOf('.');
        return (idx >= 0 && idx < code.length() - 1) ? code.substring(idx + 1) : code;
    }

    private void logClientEvent(String event, Exception ex) {
        log.info("{}: {}", event, ex.getMessage());
    }

    private void logByStatus(String event, Exception ex, HttpStatus status) {
        if (status.is4xxClientError()) {
            logClientEvent(event, ex);
        } else {
            log.error("{}: {}", event, ex.getMessage(), ex);
        }
    }
}
