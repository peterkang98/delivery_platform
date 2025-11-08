package xyz.sparta_project.manjok.domain.order.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.model.Address;
import xyz.sparta_project.manjok.domain.order.domain.model.Coordinate;
import xyz.sparta_project.manjok.domain.order.domain.model.Orderer;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrdererVO Value Object 변환 테스트")
class OrdererVOTest {

    @Test
    @DisplayName("도메인 -> VO 변환이 정상적으로 이루어진다")
    void fromDomain_success() {
        // given
        Coordinate coordinate = Coordinate.create(
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780")
        );
        Address address = Address.create(
                "서울특별시",
                "중구",
                "명동",
                "123번지",
                coordinate
        );
        Orderer domain = Orderer.create(
                "user-123",
                "홍길동",
                "010-1234-5678",
                address,
                "문 앞에 놔주세요"
        );

        // when
        OrdererVO vo = OrdererVO.from(domain);

        // then
        assertThat(vo.getUserId()).isEqualTo("user-123");
        assertThat(vo.getName()).isEqualTo("홍길동");
        assertThat(vo.getPhone()).isEqualTo("010-1234-5678");
        assertThat(vo.getDeliveryRequest()).isEqualTo("문 앞에 놔주세요");
        assertThat(vo.getAddress()).isNotNull();
    }

    @Test
    @DisplayName("VO -> 도메인 변환이 정상적으로 이루어진다")
    void toDomain_success() {
        // given
        CoordinateVO coordinateVO = CoordinateVO.builder()
                .latitude(new BigDecimal("35.1796"))
                .longitude(new BigDecimal("129.0756"))
                .build();
        AddressVO addressVO = AddressVO.builder()
                .province("부산광역시")
                .city("해운대구")
                .district("우동")
                .detailAddress("456번지")
                .coordinate(coordinateVO)
                .build();
        OrdererVO vo = OrdererVO.builder()
                .userId("user-456")
                .name("김철수")
                .phone("010-9876-5432")
                .deliveryRequest("빠른 배달 부탁합니다")
                .address(addressVO)
                .build();

        // when
        Orderer domain = vo.toDomain();

        // then
        assertThat(domain.getUserId()).isEqualTo("user-456");
        assertThat(domain.getName()).isEqualTo("김철수");
        assertThat(domain.getPhone()).isEqualTo("010-9876-5432");
        assertThat(domain.getDeliveryRequest()).isEqualTo("빠른 배달 부탁합니다");
        assertThat(domain.getAddress()).isNotNull();
    }

    @Test
    @DisplayName("도메인 -> VO -> 도메인 변환 시 값이 동일해야 한다")
    void roundTripConversion() {
        // given
        Coordinate coordinate = Coordinate.create(
                new BigDecimal("33.4996"),
                new BigDecimal("126.5312")
        );
        Address address = Address.create(
                "제주특별자치도",
                "제주시",
                "노형동",
                "789번지",
                coordinate
        );
        Orderer original = Orderer.create(
                "user-789",
                "이영희",
                "010-5555-6666",
                address,
                "조심히 다뤄주세요"
        );

        // when
        Orderer converted = OrdererVO.from(original).toDomain();

        // then
        assertThat(converted)
                .usingRecursiveComparison()
                .isEqualTo(original);
    }
}