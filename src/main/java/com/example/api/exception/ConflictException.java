package com.example.api.exception;

import java.util.List;

/**
 * リソースの重複など 409 Conflict を返すべきケースで利用する例外。
 */
public class ConflictException extends RuntimeException {
    private final transient List<FieldErrorDetail> errors;
    /**
     * 詳細エラー付き例外を生成します。
     *
     * @param message エラーメッセージ
     * @param errors  詳細エラー一覧
     */
    public ConflictException(String message, List<FieldErrorDetail> errors) { super(message); this.errors = errors; }
    /**
     * 保持している詳細エラーを返します。
     *
     * @return 詳細エラー一覧
     */
    public List<FieldErrorDetail> getErrors() { return errors; }
}
