package com.example.api.exception;

import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * エラーコード・メッセージ・理由の定義を一元管理するカタログ。
 * アプリケーション全体で共通利用するため、ここで定数化します。
 */
public final class ErrorCatalog {
    private static final Map<HttpStatus, String> DEFAULT_MESSAGES;

    static {
        EnumMap<HttpStatus, String> defaults = new EnumMap<>(HttpStatus.class);
        defaults.put(HttpStatus.BAD_REQUEST, Messages.VALIDATION_FAILED);
        defaults.put(HttpStatus.NOT_FOUND, Messages.NOT_FOUND);
        defaults.put(HttpStatus.METHOD_NOT_ALLOWED, Messages.METHOD_NOT_ALLOWED);
        defaults.put(HttpStatus.NOT_ACCEPTABLE, Messages.NOT_ACCEPTABLE);
        defaults.put(HttpStatus.UNPROCESSABLE_ENTITY, Messages.VALIDATION_FAILED);
        defaults.put(HttpStatus.INTERNAL_SERVER_ERROR, Messages.UNEXPECTED_ERROR);
        DEFAULT_MESSAGES = Collections.unmodifiableMap(defaults);
    }

    private ErrorCatalog() {
    }

    /**
     * エラーコード（HTTP応答で利用するトップレベルコード）。
     */
    public static final class Codes {
        private Codes() {
        }

        public static final String BAD_REQUEST = "BAD_REQUEST";
        public static final String NOT_FOUND = "NOT_FOUND";
        public static final String CONFLICT = "CONFLICT";
        public static final String UNPROCESSABLE_ENTITY = "UNPROCESSABLE_ENTITY";
        public static final String METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
        public static final String NOT_ACCEPTABLE = "NOT_ACCEPTABLE";
        public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    }

    /**
     * 詳細エラーのコード（フィールド単位など）。
     */
    public static final class DetailCodes {
        private DetailCodes() {
        }

        public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
        public static final String DUPLICATE = "DUPLICATE";
        public static final String INVALID_PERIOD = "INVALID_PERIOD";
    }

    /**
     * エラーメッセージ（レスポンスのmessageに設定する値）。
     */
    public static final class Messages {
        private Messages() {
        }

        public static final String VALIDATION_FAILED = "Validation failed";
        public static final String DUPLICATE_RESOURCE = "Duplicate resource";
        public static final String INVALID_PERIOD = "Invalid period";
        public static final String USER_NOT_FOUND = "User not found";
        public static final String INVALID_USER_ID = "Invalid user_id";
        public static final String MALFORMED_JSON = "Malformed JSON or invalid format";
        public static final String METHOD_NOT_ALLOWED = "Method not allowed";
        public static final String NOT_ACCEPTABLE = "Not acceptable";
        public static final String TYPE_MISMATCH = "Type mismatch";
        public static final String NOT_FOUND = "Resource not found";
        public static final String UNEXPECTED_ERROR = "Unexpected error";
    }

    /**
     * 詳細エラーの理由（FieldErrorDetail.resonに設定する値）。
     */
    public static final class Reasons {
        private Reasons() {
        }

        public static final String NAME_ALREADY_EXISTS = "name already exists";
        public static final String PERIOD_FROM_AFTER_TO = "period.from must be on/before period.to";
    }

    /**
     * FieldErrorDetailインスタンスを生成するユーティリティ。
     *
     * @param code        詳細エラーコード
     * @param reason      詳細エラー理由
     * @param field       対象フィールド
     * @param constraints 制約情報
     * @return FieldErrorDetail
     */
    public static FieldErrorDetail fieldError(String code, String reason, String field, Object constraints) {
        return new FieldErrorDetail(code, reason, field, constraints);
    }

    /**
     * HTTPステータスに紐づいたエラーコードを返します。
     *
     * @param status HTTPステータス
     * @return エラーコード
     */
    public static String code(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> Codes.BAD_REQUEST;
            case NOT_FOUND -> Codes.NOT_FOUND;
            case CONFLICT -> Codes.CONFLICT;
            case UNPROCESSABLE_ENTITY -> Codes.UNPROCESSABLE_ENTITY;
            case METHOD_NOT_ALLOWED -> Codes.METHOD_NOT_ALLOWED;
            case NOT_ACCEPTABLE -> Codes.NOT_ACCEPTABLE;
            case INTERNAL_SERVER_ERROR -> Codes.INTERNAL_SERVER_ERROR;
            default -> status.name();
        };
    }

    /**
     * HTTPステータスに対応する既定メッセージを取得します。
     *
     * @param status HTTPステータス
     * @return 既定メッセージ
     */
    public static String defaultMessage(HttpStatus status) {
        return DEFAULT_MESSAGES.getOrDefault(status, status.getReasonPhrase());
    }

    /**
     * ApiErrorResponseを生成します。
     *
     * @param status  HTTPステータス
     * @param message メッセージ（nullの場合は既定メッセージを利用）
     * @param traceId トレースID
     * @return ApiErrorResponse
     */
    public static ApiErrorResponse toResponse(HttpStatus status, String message, String traceId) {
        return toResponse(status, message, traceId, null);
    }

    /**
     * ApiErrorResponseを生成します。
     *
     * @param status  HTTPステータス
     * @param message メッセージ（nullの場合は既定メッセージを利用）
     * @param traceId トレースID
     * @param errors  詳細エラー
     * @return ApiErrorResponse
     */
    public static ApiErrorResponse toResponse(HttpStatus status, String message, String traceId, List<FieldErrorDetail> errors) {
        String resolvedMessage = (message != null && !message.isBlank()) ? message : defaultMessage(status);
        return new ApiErrorResponse(code(status), resolvedMessage, traceId, errors);
    }
}
