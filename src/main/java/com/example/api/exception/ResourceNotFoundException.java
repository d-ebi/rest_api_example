package com.example.api.exception;

/**
 * 指定したリソースが見つからない場合に投げる例外。
 * {@link org.springframework.http.HttpStatus#NOT_FOUND} に対応します。
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * 例外を生成します。
     *
     * @param message エラーメッセージ
     */
    public ResourceNotFoundException(String message) { super(message); }
}
