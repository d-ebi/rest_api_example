package com.example.api.validation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * メッセージプロパティをUTF-8で読み込むユーティリティ。
 * バリデーション用（ValidationMessages.properties）とアプリ用（messages.properties）を提供します。
 */
public final class MessageAccessor {
    private static final String VALIDATION_BUNDLE_NAME = "ValidationMessages";
    private static final String MESSAGE_BUNDLE_NAME = "messages";

    private static final ResourceBundle VALIDATION_BUNDLE = load(VALIDATION_BUNDLE_NAME);
    private static final ResourceBundle MESSAGE_BUNDLE = load(MESSAGE_BUNDLE_NAME);

    private MessageAccessor() {
    }

    /**
     * 既定のバリデーションメッセージバンドルからメッセージを取得します。
     *
     * @param key メッセージキー
     * @return 解決されたメッセージ（存在しない場合はキーを返します）
     */
    public static String get(String key) {
        return validation(key);
    }

    /**
     * {@code ValidationMessages.properties} からメッセージを取得します。
     *
     * @param key メッセージキー
     * @return 解決されたメッセージ
     */
    public static String validation(String key) {
        return resolve(VALIDATION_BUNDLE, key);
    }

    /**
     * {@code messages.properties} からメッセージを取得します。
     *
     * @param key メッセージキー
     * @return 解決されたメッセージ
     */
    public static String message(String key) {
        return resolve(MESSAGE_BUNDLE, key);
    }

    /**
     * UTF-8 対応の {@link ResourceBundle} を読み込みます。
     *
     * @param bundleName バンドル名
     * @return 読み込み結果（見つからない場合はnull）
     */
    private static ResourceBundle load(String bundleName) {
        try {
            return ResourceBundle.getBundle(bundleName, Locale.getDefault(), new Utf8Control());
        } catch (MissingResourceException ex) {
            return null;
        }
    }

    /**
     * ResourceBundle から値を解決します。
     *
     * @param bundle 対象バンドル
     * @param key    メッセージキー
     * @return 解決されたメッセージ（存在しなければキーそのもの）
     */
    private static String resolve(ResourceBundle bundle, String key) {
        if (bundle != null && bundle.containsKey(key)) {
            return bundle.getString(key);
        }
        return key;
    }

    /**
     * ResourceBundle を UTF-8 で読み込むためのカスタムコントロール。
     */
    private static class Utf8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                        ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            try (InputStream stream = loader.getResourceAsStream(resourceName)) {
                if (stream == null) return null;
                try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    return new PropertyResourceBundle(reader);
                }
            }
        }
    }
}
