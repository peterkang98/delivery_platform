package xyz.sparta_project.manjok.global.presentation.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.global.common.dto.PageInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PageResponse 테스트")
class PageResponseTest {

    @Test
    @DisplayName("정상적인 PageResponse 생성")
    void create_page_response_successfully() {
        // Given
        List<String> content = Arrays.asList("item1", "item2", "item3");
        PageInfo pageInfo = PageInfo.of(0, 10, 3, 1, 3);

        // When
        PageResponse<String> response = PageResponse.of(content, pageInfo);

        // Then
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getContent()).containsExactly("item1", "item2", "item3");
        assertThat(response.getPageInfo()).isNotNull();
        assertThat(response.getPageInfo().getPage()).isEqualTo(0);
        assertThat(response.getPageInfo().getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("빈 페이지 응답 생성")
    void create_empty_page_response() {
        // Given
        List<String> content = Collections.emptyList();
        PageInfo pageInfo = PageInfo.of(0, 10, 0, 0, 0);

        // When
        PageResponse<String> response = PageResponse.of(content, pageInfo);

        // Then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getPageInfo().isEmpty()).isTrue();
        assertThat(response.getPageInfo().getTotalElements()).isEqualTo(0);
        assertThat(response.getPageInfo().getTotalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("pageInfo가 null일 때 예외 발생")
    void throw_exception_when_page_info_is_null() {
        // Given
        List<String> content = Arrays.asList("item1", "item2");

        // When & Then
        assertThatThrownBy(() -> PageResponse.of(content, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }
}