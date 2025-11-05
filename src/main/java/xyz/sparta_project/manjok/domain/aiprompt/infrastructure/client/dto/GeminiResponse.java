package xyz.sparta_project.manjok.domain.aiprompt.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiResponse {

    private List<Candidate> candidates;
    private PromptFeedback promptFeedback;
    private UsageMetadata usageMetadata;

    @Getter
    @NoArgsConstructor
    public static class Candidate {
        private Content content;
        private String finishReason;
        private Integer index;
        private List<SafetyRating> safetyRatings;
    }

    @Getter
    @NoArgsConstructor
    public static class Content {
        private List<Part> parts;
        private String role;
    }

    @Getter
    @NoArgsConstructor
    public static class Part {
        private String text;
    }

    @Getter
    @NoArgsConstructor
    public static class SafetyRating {
        private String category;
        private String probability;
    }

    @Getter
    @NoArgsConstructor
    public static class PromptFeedback {
        private List<SafetyRating> safetyRatings;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UsageMetadata {
        private Integer promptTokenCount;
        private Integer candidatesTokenCount;
        private Integer totalTokenCount;
    }

    /**
     * 생성된 텍스트 추출
     */
    public String getGeneratedText() {
        if (candidates == null || candidates.isEmpty()) {
            return "";
        }

        Content content = candidates.get(0).getContent();
        if (content == null || content.getParts() == null || content.getParts().isEmpty()) {
            return "";
        }

        return content.getParts().get(0).getText();
    }

    /**
     * 응답이 안전한지 확인
     */
    public boolean isSafe() {
        if (candidates == null || candidates.isEmpty()) {
            return false;
        }

        List<SafetyRating> ratings = candidates.get(0).getSafetyRatings();
        if (ratings == null) {
            return true;
        }

        return ratings.stream()
                .noneMatch(rating -> "HIGH".equals(rating.getProbability()));
    }
}