package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Address;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AddressVO 변환 테스트
 */
class AddressVOTest {

    @Test
    @DisplayName("도메인 Address를 AddressVO로 변환")
    void fromDomain_ShouldConvertAddressToAddressVO() {
        // given
        Address domain = Address.builder()
                .province("서울특별시")
                .city("강남구")
                .district("역삼동")
                .detailAddress("테헤란로 123")
                .build();

        // when
        AddressVO vo = AddressVO.fromDomain(domain);

        // then
        assertThat(vo).isNotNull();
        assertThat(vo.getProvince()).isEqualTo("서울특별시");
        assertThat(vo.getCity()).isEqualTo("강남구");
        assertThat(vo.getDistrict()).isEqualTo("역삼동");
        assertThat(vo.getDetailAddress()).isEqualTo("테헤란로 123");
    }

    @Test
    @DisplayName("AddressVO를 도메인 Address로 변환")
    void toDomain_ShouldConvertAddressVOToAddress() {
        // given
        AddressVO vo = AddressVO.builder()
                .province("경기도")
                .city("성남시")
                .district("분당구")
                .detailAddress("판교역로 235")
                .build();

        // when
        Address domain = vo.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getProvince()).isEqualTo("경기도");
        assertThat(domain.getCity()).isEqualTo("성남시");
        assertThat(domain.getDistrict()).isEqualTo("분당구");
        assertThat(domain.getDetailAddress()).isEqualTo("판교역로 235");
    }
}