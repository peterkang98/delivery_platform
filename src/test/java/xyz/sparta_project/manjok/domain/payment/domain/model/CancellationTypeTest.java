package xyz.sparta_project.manjok.domain.payment.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CancellationType Enum 테스트")
class CancellationTypeTest {

    @Nested
    @DisplayName("Enum 값 확인")
    class EnumValues {

        @Test
        @DisplayName("모든 CancellationType 값이 존재한다")
        void allValuesExist() {
            CancellationType[] types = CancellationType.values();

            assertThat(types).hasSize(5);
            assertThat(types).containsExactlyInAnyOrder(
                    CancellationType.USER_REQUEST,
                    CancellationType.SYSTEM_ERROR,
                    CancellationType.ADMIN_CANCEL,
                    CancellationType.FRAUD_DETECTION,
                    CancellationType.TIMEOUT
            );
        }

        @Test
        @DisplayName("각 취소 유형은 올바른 한글 설명을 가진다")
        void descriptions() {
            assertThat(CancellationType.USER_REQUEST.getDescription()).isEqualTo("사용자 요청");
            assertThat(CancellationType.SYSTEM_ERROR.getDescription()).isEqualTo("시스템 오류");
            assertThat(CancellationType.ADMIN_CANCEL.getDescription()).isEqualTo("관리자 취소");
            assertThat(CancellationType.FRAUD_DETECTION.getDescription()).isEqualTo("부정 거래 감지");
            assertThat(CancellationType.TIMEOUT.getDescription()).isEqualTo("타임아웃");
        }
    }

    @Nested
    @DisplayName("취소 유형별 특성")
    class CancellationTypeCharacteristics {

        @Test
        @DisplayName("USER_REQUEST는 사용자 요청에 의한 취소")
        void userRequest() {
            assertThat(CancellationType.USER_REQUEST.getDescription()).isEqualTo("사용자 요청");
        }

        @Test
        @DisplayName("SYSTEM_ERROR는 시스템 오류에 의한 취소")
        void systemError() {
            assertThat(CancellationType.SYSTEM_ERROR.getDescription()).isEqualTo("시스템 오류");
        }

        @Test
        @DisplayName("ADMIN_CANCEL은 관리자에 의한 취소")
        void adminCancel() {
            assertThat(CancellationType.ADMIN_CANCEL.getDescription()).isEqualTo("관리자 취소");
        }

        @Test
        @DisplayName("FRAUD_DETECTION은 부정 거래 감지에 의한 취소")
        void fraudDetection() {
            assertThat(CancellationType.FRAUD_DETECTION.getDescription()).isEqualTo("부정 거래 감지");
        }

        @Test
        @DisplayName("TIMEOUT은 타임아웃에 의한 취소")
        void timeout() {
            assertThat(CancellationType.TIMEOUT.getDescription()).isEqualTo("타임아웃");
        }
    }

    @Nested
    @DisplayName("Enum 메서드 동작")
    class EnumMethods {

        @Test
        @DisplayName("valueOf()로 문자열을 Enum으로 변환")
        void valueOf_success() {
            CancellationType type = CancellationType.valueOf("USER_REQUEST");

            assertThat(type).isEqualTo(CancellationType.USER_REQUEST);
        }

        @Test
        @DisplayName("존재하지 않는 값으로 valueOf() 호출 시 예외")
        void valueOf_fail() {
            assertThatThrownBy(() ->
                    CancellationType.valueOf("INVALID_TYPE"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("name()으로 Enum 상수명 조회")
        void name_method() {
            assertThat(CancellationType.USER_REQUEST.name()).isEqualTo("USER_REQUEST");
            assertThat(CancellationType.SYSTEM_ERROR.name()).isEqualTo("SYSTEM_ERROR");
            assertThat(CancellationType.ADMIN_CANCEL.name()).isEqualTo("ADMIN_CANCEL");
            assertThat(CancellationType.FRAUD_DETECTION.name()).isEqualTo("FRAUD_DETECTION");
            assertThat(CancellationType.TIMEOUT.name()).isEqualTo("TIMEOUT");
        }

        @Test
        @DisplayName("switch 문에서 사용 가능")
        void switchStatement() {
            CancellationType type = CancellationType.USER_REQUEST;

            String result = switch (type) {
                case USER_REQUEST -> "사용자가 직접 취소";
                case SYSTEM_ERROR -> "시스템 장애로 취소";
                case ADMIN_CANCEL -> "운영자가 강제 취소";
                case FRAUD_DETECTION -> "이상 거래 탐지";
                case TIMEOUT -> "결제 시간 초과";
            };

            assertThat(result).isEqualTo("사용자가 직접 취소");
        }
    }

    @Nested
    @DisplayName("취소 유형 분류")
    class CancellationTypeClassification {

        @Test
        @DisplayName("사용자 주도 취소")
        void userInitiated() {
            CancellationType type = CancellationType.USER_REQUEST;

            assertThat(type.getDescription()).contains("사용자");
        }

        @Test
        @DisplayName("시스템 주도 취소")
        void systemInitiated() {
            assertThat(CancellationType.SYSTEM_ERROR.getDescription()).contains("시스템");
            assertThat(CancellationType.FRAUD_DETECTION.getDescription()).contains("부정");
            assertThat(CancellationType.TIMEOUT.getDescription()).contains("타임아웃");
        }

        @Test
        @DisplayName("관리자 주도 취소")
        void adminInitiated() {
            CancellationType type = CancellationType.ADMIN_CANCEL;

            assertThat(type.getDescription()).contains("관리자");
        }
    }

    @Test
    @DisplayName("취소 유형 동등성 비교")
    void equality() {
        CancellationType type1 = CancellationType.USER_REQUEST;
        CancellationType type2 = CancellationType.USER_REQUEST;
        CancellationType type3 = CancellationType.ADMIN_CANCEL;

        assertThat(type1).isEqualTo(type2);
        assertThat(type1 == type2).isTrue();
        assertThat(type1).isNotEqualTo(type3);
    }

    @Test
    @DisplayName("취소 시나리오별 유형 선택")
    void cancellationScenarios() {
        // 고객 변심
        CancellationType userCancel = CancellationType.USER_REQUEST;
        assertThat(userCancel).isEqualTo(CancellationType.USER_REQUEST);

        // 결제 시스템 장애
        CancellationType systemError = CancellationType.SYSTEM_ERROR;
        assertThat(systemError).isEqualTo(CancellationType.SYSTEM_ERROR);

        // 운영팀 판단으로 취소
        CancellationType adminCancel = CancellationType.ADMIN_CANCEL;
        assertThat(adminCancel).isEqualTo(CancellationType.ADMIN_CANCEL);

        // 이상 거래 패턴 감지
        CancellationType fraud = CancellationType.FRAUD_DETECTION;
        assertThat(fraud).isEqualTo(CancellationType.FRAUD_DETECTION);

        // 결제 승인 대기 시간 초과
        CancellationType timeout = CancellationType.TIMEOUT;
        assertThat(timeout).isEqualTo(CancellationType.TIMEOUT);
    }
}