package xyz.sparta_project.manjok.domain.payment.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PaymentMethod Enum 테스트")
class PaymentMethodTest {

    @Nested
    @DisplayName("Enum 값 확인")
    class EnumValues {

        @Test
        @DisplayName("모든 PaymentMethod 값이 존재한다")
        void allValuesExist() {
            PaymentMethod[] methods = PaymentMethod.values();

            assertThat(methods).hasSize(5);
            assertThat(methods).containsExactlyInAnyOrder(
                    PaymentMethod.CARD,
                    PaymentMethod.VIRTUAL_ACCOUNT,
                    PaymentMethod.TRANSFER,
                    PaymentMethod.MOBILE,
                    PaymentMethod.EASY_PAY
            );
        }

        @Test
        @DisplayName("각 결제 수단은 올바른 한글 설명을 가진다")
        void descriptions() {
            assertThat(PaymentMethod.CARD.getDescription()).isEqualTo("카드");
            assertThat(PaymentMethod.VIRTUAL_ACCOUNT.getDescription()).isEqualTo("가상계좌");
            assertThat(PaymentMethod.TRANSFER.getDescription()).isEqualTo("계좌이체");
            assertThat(PaymentMethod.MOBILE.getDescription()).isEqualTo("휴대폰");
            assertThat(PaymentMethod.EASY_PAY.getDescription()).isEqualTo("간편결제");
        }
    }

    @Nested
    @DisplayName("결제 수단별 특성")
    class PaymentMethodCharacteristics {

        @Test
        @DisplayName("CARD는 카드 결제")
        void card() {
            assertThat(PaymentMethod.CARD.getDescription()).isEqualTo("카드");
        }

        @Test
        @DisplayName("VIRTUAL_ACCOUNT는 가상계좌 결제")
        void virtualAccount() {
            assertThat(PaymentMethod.VIRTUAL_ACCOUNT.getDescription()).isEqualTo("가상계좌");
        }

        @Test
        @DisplayName("TRANSFER는 계좌이체 결제")
        void transfer() {
            assertThat(PaymentMethod.TRANSFER.getDescription()).isEqualTo("계좌이체");
        }

        @Test
        @DisplayName("MOBILE은 휴대폰 결제")
        void mobile() {
            assertThat(PaymentMethod.MOBILE.getDescription()).isEqualTo("휴대폰");
        }

        @Test
        @DisplayName("EASY_PAY는 간편결제")
        void easyPay() {
            assertThat(PaymentMethod.EASY_PAY.getDescription()).isEqualTo("간편결제");
        }
    }

    @Nested
    @DisplayName("Enum 메서드 동작")
    class EnumMethods {

        @Test
        @DisplayName("valueOf()로 문자열을 Enum으로 변환")
        void valueOf_success() {
            PaymentMethod method = PaymentMethod.valueOf("CARD");

            assertThat(method).isEqualTo(PaymentMethod.CARD);
        }

        @Test
        @DisplayName("존재하지 않는 값으로 valueOf() 호출 시 예외")
        void valueOf_fail() {
            assertThatThrownBy(() ->
                    PaymentMethod.valueOf("INVALID_METHOD"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("name()으로 Enum 상수명 조회")
        void name_method() {
            assertThat(PaymentMethod.CARD.name()).isEqualTo("CARD");
            assertThat(PaymentMethod.VIRTUAL_ACCOUNT.name()).isEqualTo("VIRTUAL_ACCOUNT");
            assertThat(PaymentMethod.TRANSFER.name()).isEqualTo("TRANSFER");
            assertThat(PaymentMethod.MOBILE.name()).isEqualTo("MOBILE");
            assertThat(PaymentMethod.EASY_PAY.name()).isEqualTo("EASY_PAY");
        }

        @Test
        @DisplayName("switch 문에서 사용 가능")
        void switchStatement() {
            PaymentMethod method = PaymentMethod.CARD;

            String result = switch (method) {
                case CARD -> "신용/체크카드";
                case VIRTUAL_ACCOUNT -> "가상계좌입금";
                case TRANSFER -> "실시간계좌이체";
                case MOBILE -> "휴대폰소액결제";
                case EASY_PAY -> "간편결제서비스";
            };

            assertThat(result).isEqualTo("신용/체크카드");
        }
    }

    @Test
    @DisplayName("결제 수단 동등성 비교")
    void equality() {
        PaymentMethod method1 = PaymentMethod.CARD;
        PaymentMethod method2 = PaymentMethod.CARD;
        PaymentMethod method3 = PaymentMethod.TRANSFER;

        assertThat(method1).isEqualTo(method2);
        assertThat(method1 == method2).isTrue();
        assertThat(method1).isNotEqualTo(method3);
    }
}