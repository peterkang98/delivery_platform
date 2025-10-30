package xyz.sparta_project.manjok.global.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PageInfo 테스트")
class PageInfoTest {

    @Test
    @DisplayName("정상적인 중간 페이지 정보 생성")
    void creat_middle_pageInfo() {
        // Given
        int page = 2;
        int size = 10;
        long totalElements = 100;
        int totalPages = 10;
        int numberOfElements = 10;

        // When
        PageInfo pageInfo = PageInfo.of(
                page,
                size,
                totalElements,
                totalPages,
                numberOfElements
        );

        // That
        assertThat(pageInfo.getPage()).isEqualTo(2);
        assertThat(pageInfo.getSize()).isEqualTo(10);
        assertThat(pageInfo.getTotalElements()).isEqualTo(100);
        assertThat(pageInfo.getTotalPages()).isEqualTo(10);
        assertThat(pageInfo.getNumberOfElements()).isEqualTo(10);
        assertThat(pageInfo.isFirst()).isFalse();
        assertThat(pageInfo.isLast()).isFalse();
        assertThat(pageInfo.isHasNext()).isTrue();
        assertThat(pageInfo.isHasPrevious()).isTrue();
        assertThat(pageInfo.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("첫 페이지 플래그 검증")
    void create_firs_pageInfo() {
        //Given
        int page = 0;
        int size = 10;
        int totalElements = 100;
        int totalPages = 10;
        int numberOfElements = 10;

        //When
        PageInfo pageInfo = PageInfo.of(
                page,
                size,
                totalElements,
                totalPages,
                numberOfElements
        );

        //Then
        assertThat(pageInfo.isFirst()).isTrue();
        assertThat(pageInfo.isHasPrevious()).isFalse();
        assertThat(pageInfo.isLast()).isFalse();
        assertThat(pageInfo.isHasNext()).isTrue();
    }
    
    @Test
    @DisplayName("마지막 페이지 플래그 검증")
    void creat_last_pageInfo() {
        //Given
        int page = 9;
        int size = 10;
        long totalElements = 100;
        int totalPages = 10;
        int numberOfElements = 10;

        //When
        PageInfo pageInfo = PageInfo.of(
                page,
                size,
                totalElements,
                totalPages,
                numberOfElements
        );
    }

    @Test
    @DisplayName("음수 값으로 생성 시 예외 발생")
    void creat_with_negative_values() {
        //Given & When & Then
        assertThatThrownBy(() -> PageInfo.of(-1, 10, 100, 10, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page");

        assertThatThrownBy(() -> PageInfo.of(0, 0, 100, 10, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size");

        assertThatThrownBy(() -> PageInfo.of(0, 19, -1, 10, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalElements");

        assertThatThrownBy(() -> PageInfo.of(0, 10, 100, -1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalPages");


    }

}