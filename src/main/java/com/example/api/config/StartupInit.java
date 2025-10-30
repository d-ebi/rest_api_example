package com.example.api.config;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * アプリ起動時にデータディレクトリを作成します。
 */
@Component
public class StartupInit {
    /**
     * アプリケーションコンテキスト初期化時にデータディレクトリ（./data）を確保します。
     */
    @EventListener(ContextRefreshedEvent.class)
    public void ensureDataDir() {
        try {
            Path data = Path.of("data");
            if (!Files.exists(data)) {
                Files.createDirectories(data);
            }
        } catch (Exception ignored) {}
    }
}
