package xyz.sparta_project.manjok.domain.order.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Address Value Object 테스트")
class AddressTest {

    private Coordinate coordinate;

    @BeforeEach
    void setUp() {
        // given: 유효한 좌표 객체 준비
        coordinate = Coordinate.create(
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780")
        );
    }

    @Nested
    @DisplayName("create() - 주소 생성")
    class CreateAddress {

        @Test
        @DisplayName("정상적인 값으로 Address 생성에 성공한다")
        void create_success() {
            // given
            String province = "서울특별시";
            String city = "강남구";
            String district = "역삼동";
            String detail = "테헤란로 123";

            // when
            Address address = Address.create(province, city, district, detail, coordinate);

            // then
            assertThat(address.getProvince()).isEqualTo(province);
            assertThat(address.getCity()).isEqualTo(city);
            assertThat(address.getDistrict()).isEqualTo(district);
            assertThat(address.getDetailAddress()).isEqualTo(detail);
            assertThat(address.getCoordinate()).isEqualTo(coordinate);
        }

        @Test
        @DisplayName("province 가 null 또는 공백이면 예외가 발생한다")
        void create_fail_province() {
            // when / then
            assertThatThrownBy(() ->
                    Address.create(" ", "강남구", "역삼동", "테헤란로 123", coordinate)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("city 가 null 또는 공백이면 예외가 발생한다")
        void create_fail_city() {
            assertThatThrownBy(() ->
                    Address.create("서울", "", "역삼동", "테헤란로 123", coordinate)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("district 가 null 또는 공백이면 예외가 발생한다")
        void create_fail_district() {
            assertThatThrownBy(() ->
                    Address.create("서울", "강남구", " ", "테헤란로 123", coordinate)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("detailAddress 가 null 또는 공백이면 예외가 발생한다")
        void create_fail_detailAddress() {
            assertThatThrownBy(() ->
                    Address.create("서울", "강남구", "역삼동", "", coordinate)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("coordinate 가 null 이면 예외가 발생한다")
        void create_fail_coordinate() {
            assertThatThrownBy(() ->
                    Address.create("서울", "강남구", "역삼동", "테헤란로 123", null)
            ).isInstanceOf(OrderException.class);
        }
    }

    @Nested
    @DisplayName("getFullAddress() - 전체 주소 문자열 반환")
    class FullAddress {

        @Test
        @DisplayName("전체 주소 문자열을 올바르게 반환한다")
        void getFullAddress_success() {
            // given
            Address address = Address.create("서울", "강남구", "역삼동", "테헤란로 123", coordinate);

            // when
            String fullAddress = address.getFullAddress();

            // then
            assertThat(fullAddress).isEqualTo("서울 강남구 역삼동 테헤란로 123");
        }
    }
}
