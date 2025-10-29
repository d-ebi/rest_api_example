package com.example.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPIのグローバルメタ情報設定。
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Users API",
                version = "v1",
                description = "ユーザー管理REST API (JSON, SQLite)"
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local")
        }
)
public class OpenApiConfig { }
