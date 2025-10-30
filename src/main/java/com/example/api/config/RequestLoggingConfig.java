package com.example.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * リクエストの概要をINFOで出力するインターセプタ設定。
 */
@Configuration
public class RequestLoggingConfig implements WebMvcConfigurer {
    /**
     * リクエスト監視用インターセプタをMVCへ登録します。
     *
     * @param registry インターセプタレジストリ
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestLoggingInterceptor());
    }

    /**
     * リクエスト開始／終了をINFOレベルで出力するインターセプタ。
     */
    static class RequestLoggingInterceptor implements HandlerInterceptor {
        private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

        /**
         * ハンドラ実行前にメソッド・URI・クエリを記録します。
         */
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String query = request.getQueryString();
            log.info("Incoming request: method={} uri={}{}", method, uri, (query != null ? ("?" + query) : ""));
            return true;
        }

        /**
         * ハンドラ完了後にステータスと例外の有無を出力します。
         */
        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
            int status = response.getStatus();
            log.info("Completed request: uri={} status={}", request.getRequestURI(), status);
            if (ex != null) {
                log.error("Request raised exception: {}", ex.getMessage(), ex);
            }
        }
    }
}
