package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Address 값 객체 테스트")
class AddressTest {
    
    @Test
    @DisplayName("전체 주소를 조합할 수 있다.")
    void should_combine_full_address() {
        // given
        Address address = Address.builder()
                .province("서울특별시")
                .city("종로구")
                .district("광화문동")
                .detailAddress("123-4번지 2층")
                .build();

        // when
        String fullAddress = address.getFullAddress();

        // then
        assertThat(fullAddress).isEqualTo("서울특별시 종로구 광화문동 123-4번지 2층");
    }
    
    @Test
    @DisplayName("상세주소가 없어도 전체 주소를 조합할 수 있다.")
    void should_combine_full_address_without_detail() {
        // given
        Address address = Address.builder()
                .province("서울특별시")
                .city("종로구")
                .district("광화문동")
                .build();

        // when
        String fullAddress = address.getFullAddress();

        // then
        assertThat(fullAddress).isEqualTo("서울특별시 종로구 광화문동");
    }
    
    @Test
    @DisplayName("지역 필터링용 주소를 반환할 수 있다.")
    void should_return_area_address() {
        // given
        Address address = Address.builder()
                .province("서울특별시")
                .city("종로구")
                .district("광화문동")
                .detailAddress("123-4번지 2층")
                .build();

        // when
        String areaAddress = address.getAreaAddress();

        // then
        assertThat(areaAddress).isEqualTo("서울특별시 종로구 광화문동");
    }
    
    @Test
    @DisplayName("필수 주소 정보가 모두 있으면 유효하다.")
    void should_be_valid_with_required_fields() {
        
    }
    
    @Test
    @DisplayName("필수 주소 정보가 없으면 유효하지 않다.")
    void should_be_invalid_without_required_fields() {
        // given
        Address validAddress = Address.builder()
                .province("서울특별시")
                .city("종로구")
                .district("광화문동")
                .build();

        // when & then
        assertThat(validAddress.isValid()).isTrue();
    }
    
    @Test
    @DisplayName("값 객체는 동등성 비교가 가능하다.")
    void should_compare_equality() {
        // given
        Address noProvince = Address.builder()
                .city("종로구")
                .district("광화문동")
                .build();

        Address noCity = Address.builder()
                .province("서울특별시")
                .district("광화문동")
                .build();

        Address noDistrict = Address.builder()
                .province("서울특별시")
                .city("종로구")
                .build();

        Address emptyProvince = Address.builder()
                .province("")
                .city("종로구")
                .district("광화문동")
                .build();

        // when & then
        assertThat(noProvince.isValid()).isFalse();
        assertThat(noCity.isValid()).isFalse();
        assertThat(noDistrict.isValid()).isFalse();
        assertThat(emptyProvince.isValid()).isFalse();
    }

}