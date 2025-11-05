package xyz.sparta_project.manjok.domain.aiprompt.infrastructure.client.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GeminiRequest {

    private List<Content> contents;
    private GenerationConfig generationConfig;

    @Getter
    @Builder
    public static class Content {
        private List<Part> parts;
    }

    @Getter
    @Builder
    public static class Part {
        private String text;
    }

    @Getter
    @Builder
    public static class GenerationConfig {
        private Integer maxOutputTokens;  // 최대 출력 토큰 수
        private Double temperature;        // 창의성 (0.0 ~ 1.0)
        private Integer topK;              // Top-K 샘플링
        private Double topP;               // Top-P 샘플링
    }

    /**
     * 프롬프트로부터 요청 생성 (100줄 제한)
     */
    public static GeminiRequest of(String prompt) {
        return GeminiRequest.builder()
                .contents(List.of(
                        Content.builder()
                                .parts(List.of(
                                        Part.builder()
                                                .text(prompt)
                                                .build()
                                ))
                                .build()
                ))
                .generationConfig(GenerationConfig.builder()
                        .maxOutputTokens(4000)  // 약 100줄 정도
                        .temperature(0.7)        // 적절한 창의성
                        .topK(40)
                        .topP(0.95)
                        .build())
                .build();
    }
}