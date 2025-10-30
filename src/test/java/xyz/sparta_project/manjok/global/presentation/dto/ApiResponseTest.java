package xyz.sparta_project.manjok.global.presentation.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.global.common.dto.PageInfo;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiResponse 테스트")
class ApiResponseTest {

    @Test
    @DisplayName("데이터와 함께 성공 응답 생성")
    void create_success_response_with_data() {
        // Given
        String data = "test data";

        // When
        ApiResponse<String> response = ApiResponse.success(data);

        //Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("test data");
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("메시지와 함께 성공 응답 생성")
    void create_success_response_with_data_and_message() {
        // Given
        String data = "test data";
        String message = "요청이 성공적으로 처리되었습니다.";

        // When
        ApiResponse<String> response = ApiResponse.success(data, message);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("test data");
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }
    
    @Test
    @DisplayName("데이터 없이 성공 응답 생성")
    void create_success_response_without_data() {
        // When
        ApiResponse<Void> response = ApiResponse.success();

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }
    
    @Test
    @DisplayName("실패 응답 생성")
    void create_fail_response_with_message() {
        // Given
        String message = "요청 처리에 실패했습니다.";

        // When
        ApiResponse<Void> response = ApiResponse.fail(message);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("에러 코드와 함께 실패 응답 생성")
    void create_error_response_with_error_code() {
        // Given
        String errorCode = "E001";
        String message = "서버 내부 오류가 발생했습니다.";

        // When
        ApiResponse<Void> response = ApiResponse.error(errorCode, message);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getErrorCode()).isEqualTo(errorCode);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("PageResponse를 포함한 성공 응답 생성")
    void create_success_response_with_page_response() {
        // Given
        List<String> content = Arrays.asList("item1", "item2", "item3");
        PageInfo pageInfo = PageInfo.of(0, 10, 3, 1, 3);
        PageResponse<String> pageResponse = PageResponse.of(content, pageInfo);

        // When
        ApiResponse<PageResponse<String>> response = ApiResponse.success(pageResponse);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getContent()).hasSize(3);
        assertThat(response.getData().getPageInfo().getTotalElements()).isEqualTo(3);
    }
}