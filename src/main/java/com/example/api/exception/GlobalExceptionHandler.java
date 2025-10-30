package com.example.api.exception;

import com.example.api.config.TraceIdFilter;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.validation.FieldError;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.metadata.ConstraintDescriptor;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 例外の集約ハンドラ。
 * 仕様に基づくHTTPステータスと共通エラーレスポンスを返却します。
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String LOCATION_BODY = "body";
    private static final String LOCATION_QUERY = "query";
    private static final String LOCATION_PATH = "path";

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
                        LOCATION_BODY,
                        extractConstraints(err)))
                .collect(Collectors.toList());
        HttpStatus status = resolveStatusForFieldErrors(ex.getBindingResult().getFieldErrors());
        String message = status == HttpStatus.BAD_REQUEST
                ? ErrorCatalog.Messages.BAD_REQUEST_TOP
                : ErrorCatalog.Messages.UNPROCESSABLE_TOP;
        return response(status, message, details);
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
                        resolveLocationForViolation(v),
                        extractConstraints(v)))
                .collect(Collectors.toList());
        HttpStatus status = resolveStatusForViolations(violations);
        String message = status == HttpStatus.BAD_REQUEST
                ? ErrorCatalog.Messages.BAD_REQUEST_TOP
                : ErrorCatalog.Messages.UNPROCESSABLE_TOP;
        return response(status, message, details);
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
        return response(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCatalog.Messages.UNPROCESSABLE_TOP, ex.getErrors());
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
        Map<String, Object> constraints = Map.of(
                "invalidValue", String.valueOf(ex.getValue()),
                "expectedType", ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );
        List<FieldErrorDetail> details = List.of(ErrorCatalog.fieldError(
                ErrorCatalog.DetailCodes.VALIDATION_ERROR,
                ErrorCatalog.Messages.TYPE_MISMATCH,
                ex.getName(),
                resolveLocationForParameter(ex.getParameter()),
                constraints));
        return response(HttpStatus.BAD_REQUEST, ErrorCatalog.Messages.BAD_REQUEST_TOP, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        logClientEvent("Message not readable", ex);
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormat) {
            List<FieldErrorDetail> details = buildInvalidFormatDetails(invalidFormat);
            return response(HttpStatus.BAD_REQUEST, ErrorCatalog.Messages.BAD_REQUEST_TOP, details);
        }
        List<FieldErrorDetail> details = List.of(ErrorCatalog.fieldError(
                ErrorCatalog.DetailCodes.VALIDATION_ERROR,
                ErrorCatalog.Messages.MALFORMED_JSON,
                null,
                LOCATION_BODY,
                null));
        return response(HttpStatus.BAD_REQUEST, ErrorCatalog.Messages.BAD_REQUEST_TOP, details);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandler(NoHandlerFoundException ex) {
        logClientEvent("No handler found", ex);
        return response(HttpStatus.NOT_FOUND, ErrorCatalog.Messages.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException ex) {
        logClientEvent("Bad request", ex);
        List<FieldErrorDetail> details = ex.getErrors();
        if (details == null || details.isEmpty()) {
            details = List.of(ErrorCatalog.fieldError(
                    ErrorCatalog.DetailCodes.VALIDATION_ERROR,
                    ex.getMessage(),
                    null,
                    LOCATION_PATH,
                    null));
        }
        return response(HttpStatus.BAD_REQUEST, ErrorCatalog.Messages.BAD_REQUEST_TOP, details);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = ex.getStatus();
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

    private Map<String, Object> extractConstraints(FieldError error) {
        if (error == null) {
            return null;
        }
        try {
            ConstraintViolation<?> violation = error.unwrap(ConstraintViolation.class);
            return extractConstraints(violation);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private Map<String, Object> extractConstraints(ConstraintViolation<?> violation) {
        if (violation == null) {
            return null;
        }
        ConstraintDescriptor<?> descriptor = violation.getConstraintDescriptor();
        if (descriptor == null || descriptor.getAnnotation() == null) {
            return null;
        }
        Class<?> annotationType = descriptor.getAnnotation().annotationType();
        Map<String, Object> attributes = descriptor.getAttributes();
        if (Min.class.equals(annotationType)) {
            return Map.of("min", attributes.get("value"));
        }
        if (Max.class.equals(annotationType)) {
            return Map.of("max", attributes.get("value"));
        }
        if (Digits.class.equals(annotationType)) {
            return Map.of(
                    "integer", attributes.get("integer"),
                    "fraction", attributes.get("fraction"));
        }
        if (Size.class.equals(annotationType)) {
            return Map.of(
                    "min", attributes.get("min"),
                    "max", attributes.get("max"));
        }
        if (Pattern.class.equals(annotationType)) {
            return Map.of(
                    "pattern", attributes.get("regexp"),
                    "flags", attributes.get("flags"));
        }
        return null;
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

    private List<FieldErrorDetail> buildInvalidFormatDetails(InvalidFormatException ex) {
        String invalidValue = String.valueOf(ex.getValue());
        String expectedType = ex.getTargetType() != null ? ex.getTargetType().getSimpleName() : "unknown";
        Map<String, Object> constraints = Map.of(
                "invalidValue", invalidValue,
                "expectedType", expectedType
        );
        String fieldPath = ex.getPath().stream()
                .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                .collect(Collectors.joining("."))
                .replace(".[", "[");
        if (fieldPath.isBlank()) {
            fieldPath = "payload";
        }
        return List.of(ErrorCatalog.fieldError(
                ErrorCatalog.DetailCodes.VALIDATION_ERROR,
                ErrorCatalog.Messages.INVALID_INPUT_FORMAT,
                fieldPath,
                LOCATION_BODY,
                constraints));
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
        log.info("{}: {} api_code={}", event, ex.getMessage(), currentApiCode());
    }

    private void logByStatus(String event, Exception ex, HttpStatus status) {
        if (status.is4xxClientError()) {
            logClientEvent(event, ex);
        } else {
            log.error("{}: {} api_code={}", event, ex.getMessage(), currentApiCode(), ex);
        }
    }

    private String currentApiCode() {
        String code = MDC.get("api_code");
        return code != null ? code : "";
    }

    private String resolveLocationForViolation(ConstraintViolation<?> violation) {
        javax.validation.Path path = violation.getPropertyPath();
        for (javax.validation.Path.Node node : path) {
            if (node.getKind() == ElementKind.PARAMETER) {
                return LOCATION_QUERY;
            }
        }
        return LOCATION_QUERY;
    }

    private String resolveLocationForParameter(MethodParameter parameter) {
        if (parameter == null) return LOCATION_QUERY;
        if (parameter.hasParameterAnnotation(PathVariable.class)) return LOCATION_PATH;
        if (parameter.hasParameterAnnotation(RequestBody.class)) return LOCATION_BODY;
        if (parameter.hasParameterAnnotation(RequestParam.class)) return LOCATION_QUERY;
        return LOCATION_QUERY;
    }
}
