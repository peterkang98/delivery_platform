// infrastructure/config/RestTemplateConfig.java
package xyz.sparta_project.manjok.domain.aiprompt.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate 설정
 * - Gemini API 호출용 RestTemplate 구성
 * - Spring Boot 3.4.0+ 호환
 */
@Configuration
public class RestTemplateConfig {

    @Value("${gemini.api.timeout.connect:10000}")
    private int connectTimeout;

    @Value("${gemini.api.timeout.read:60000}")
    private int readTimeout;

    @Bean
    public RestTemplate geminiRestTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(() -> createRequestFactory())
                .build();
    }

    /**
     * ClientHttpRequestFactory 생성
     * - Timeout 설정 포함
     */
    private BufferingClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        factory.setReadTimeout(Duration.ofMillis(readTimeout));

        // 응답 본문을 여러 번 읽을 수 있도록 BufferingClientHttpRequestFactory로 래핑
        return new BufferingClientHttpRequestFactory(factory);
    }
}