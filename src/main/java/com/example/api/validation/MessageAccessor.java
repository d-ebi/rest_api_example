package com.example.api.validation;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * ValidationMessages.properties からメッセージを取得するユーティリティ。
 */
public final class MessageAccessor {
    private static final String BUNDLE_NAME = "ValidationMessages";
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());

    private MessageAccessor() {
    }

    public static String get(String key) {
        if (BUNDLE.containsKey(key)) {
            return BUNDLE.getString(key);
        }
        return key;
    }
}
