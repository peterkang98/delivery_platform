package xyz.sparta_project.manjok.global.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;

@DisplayName("UuidUtils")
class UuidUtilsTest {

    @Test
    @DisplayName("36자 UUID를 생성한다.")
    void generate_create_36_char_uuid() {
        //When
        String uuid = UuidUtils.generate();

        //Then
        assertThat(uuid).hasSize(36);
    }

    @Test
    @DisplayName("생성된 UUID는 표준 형식이다.")
    void generate_creates_valid_format() {
        //When
        String uuid = UuidUtils.generate();

        //Then
        assertThat(uuid).matches(
                "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
        );
    }

    @Test
    @DisplayName("매번 다른 UUID를 생성한다.")
    void generate_creates_unique_uuids() {
        //When
        String uuid1 = UuidUtils.generate();
        String uuid2 = UuidUtils.generate();

        //Then
        assertThat(uuid1).isNotEqualTo(uuid2);
    }

    @Test
    @DisplayName("생성된 UUID는 유효하다.")
    void generate_uuid_is_valid() {
        //When
        String uuid = UuidUtils.generate();

        //Then
        assertThatCode(() -> UUID.fromString(uuid))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("유효한 UUID를 검증한다.")
    void isValid_returns_true_for_valid_uuid() {
        //Given
        String validUuid = "550e8400-e29b-41d4-a716-44655440000";

        //When
        boolean result = UuidUtils.isValid(validUuid);

        //Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("생성된 UUID는 검증을 통과한다.")
    void generated_uuid_passes_validation() {
        //Given
        String uuid = UuidUtils.generate();

        //When
        boolean result = UuidUtils.isValid(uuid);

        //Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("잘못된 형식은 false를 반환한다.")
    void isValid_returns_false_for_isvalid_format() {
        //Given
        String invalidUuid = "invalid-uuid";

        //When
        boolean result = UuidUtils.isValid(invalidUuid);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("빈 문자열은 false를 반환한다.")
    void isValid_returns_false_for_empty_string() {
        //When
        boolean result = UuidUtils.isValid("");

        //Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("유틸리티 클래스는 인스턴스화할 수 없다.")
    void utility_class_cannot_be_instantiated() throws Exception {
        //When
        var constructor = UuidUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        Throwable thrown = catchThrowable(constructor::newInstance);

        assertThat(thrown)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(AssertionError.class)
                .hasRootCauseMessage("유틸리티 클래스는 인스턴스화 할 수 없습니다.");
    }


}