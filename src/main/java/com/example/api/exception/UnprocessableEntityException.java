package com.example.api.exception;

import java.util.List;

/**
 * 422 Unprocessable Entity を表すビジネスルール違反の例外。
 */
public class UnprocessableEntityException extends RuntimeException {
    private final transient List<FieldErrorDetail> errors;
    /**
     * 詳細エラー付き例外を生成します。
     *
     * @param message エラーメッセージ
     * @param errors  詳細エラー
     */
    public UnprocessableEntityException(String message, List<FieldErrorDetail> errors) { super(message); this.errors = errors; }
    /**
     * 保持している詳細エラーを返します。
     *
     * @return 詳細エラー
     */
    public List<FieldErrorDetail> getErrors() { return errors; }
}
