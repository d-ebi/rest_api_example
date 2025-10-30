package com.example.api.exception;

import java.util.List;

/**
 * パスパラメータやリクエスト形式が不正な場合に投げる例外。
 * 409/422 と同様に詳細エラーを併せて保持できます。
 */
public class BadRequestException extends RuntimeException {
    private final transient List<FieldErrorDetail> errors;
    /**
     * 詳細エラーなしの例外を生成します。
     *
     * @param message エラーメッセージ
     */
    public BadRequestException(String message) { super(message); this.errors = null; }
    /**
     * 詳細エラー付き例外を生成します。
     *
     * @param message エラーメッセージ
     * @param errors  詳細エラー
     */
    public BadRequestException(String message, List<FieldErrorDetail> errors) { super(message); this.errors = errors; }
    /**
     * 保持している詳細エラーを返します。
     *
     * @return 詳細エラー（存在しない場合はnull）
     */
    public List<FieldErrorDetail> getErrors() { return errors; }
}
