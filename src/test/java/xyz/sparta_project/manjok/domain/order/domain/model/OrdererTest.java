package xyz.sparta_project.manjok.domain.order.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Orderer Value Object 테스트")
class OrdererTest {

    private Address createAddress() {
        return Address.create(
                "서울특별시",
                "강남구",
                "역삼동",
                "테헤란로 123",
                Coordinate.create(
                        BigDecimal.valueOf(37.498095),
                        BigDecimal.valueOf(127.027610)
                )
        );
    }

    @Nested
    @DisplayName("create() - 주문자 생성")
    class CreateOrderer {

        @Test
        @DisplayName("정상적인 값으로 Orderer 생성에 성공한다")
        void create_success() {
            // given
            Address address = createAddress();

            // when
            Orderer orderer = Orderer.create(
                    "user001",
                    "홍길동",
                    "010-1234-5678",
                    address,
                    "문 앞에 두고 벨 눌러주세요"
            );

            // then
            assertThat(orderer.getUserId()).isEqualTo("user001");
            assertThat(orderer.getName()).isEqualTo("홍길동");
            assertThat(orderer.getPhone()).isEqualTo("010-1234-5678");
            assertThat(orderer.getAddress()).isEqualTo(address);
            assertThat(orderer.getDeliveryRequest()).isEqualTo("문 앞에 두고 벨 눌러주세요");
        }

        @Test
        @DisplayName("deliveryRequest가 null이어도 생성에 성공한다")
        void create_success_deliveryRequestNull() {
            // given
            Address address = createAddress();

            // when
            Orderer orderer = Orderer.create(
                    "user001",
                    "홍길동",
                    "010-1234-5678",
                    address,
                    null
            );

            // then
            assertThat(orderer.getDeliveryRequest()).isNull();
        }

        @Test
        @DisplayName("userId가 null 또는 공백이면 예외가 발생한다")
        void create_fail_invalidUserId() {
            Address address = createAddress();

            assertThatThrownBy(() ->
                    Orderer.create(null, "홍길동", "010-1234-5678", address, null)
            ).isInstanceOf(OrderException.class);

            assertThatThrownBy(() ->
                    Orderer.create("   ", "홍길동", "010-1234-5678", address, null)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("주문자 이름이 null 또는 공백이면 예외가 발생한다")
        void create_fail_invalidName() {
            Address address = createAddress();

            assertThatThrownBy(() ->
                    Orderer.create("user001", " ", "010-1234-5678", address, null)
            ).isInstanceOf(OrderException.class);

            assertThatThrownBy(() ->
                    Orderer.create("user001", null, "010-1234-5678", address, null)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("연락처가 null 또는 공백이면 예외가 발생한다")
        void create_fail_invalidPhone() {
            Address address = createAddress();

            assertThatThrownBy(() ->
                    Orderer.create("user001", "홍길동", null, address, null)
            ).isInstanceOf(OrderException.class);

            assertThatThrownBy(() ->
                    Orderer.create("user001", "홍길동", "   ", address, null)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("주소가 null이면 예외가 발생한다")
        void create_fail_nullAddress() {
            assertThatThrownBy(() ->
                    Orderer.create("user001", "홍길동", "010-1234-5678", null, null)
            ).isInstanceOf(OrderException.class);
        }
    }
}
