package com.example.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * アプリケーションエントリーポイント。
 * コマンドラインからの実行時に、`spring-boot.run.profiles` システムプロパティが指定されていれば
 * 追加の Spring プロファイルとして起動します。
 */
@SpringBootApplication(scanBasePackages = {"com.example.api"})
public class RestApiExampleApplication {
    /**
     * Spring Boot アプリケーションを起動します。
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RestApiExampleApplication.class);
        String runProfiles = System.getProperty("spring-boot.run.profiles");
        if (runProfiles != null && !runProfiles.isBlank()) {
            String[] profiles = java.util.Arrays.stream(runProfiles.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
            if (profiles.length > 0) {
                app.setAdditionalProfiles(profiles);
            }
        }
        app.run(args);
    }
}
