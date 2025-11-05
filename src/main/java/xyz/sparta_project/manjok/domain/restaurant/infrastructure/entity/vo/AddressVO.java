package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Address;

/**
 * Address Value Object (Embeddable)
 * - 주소 정보를 표현하는 불변 객체
 * - 여러 Entity에서 재사용 가능
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class AddressVO {

    @Column(name = "province", length = 50)
    private String province;       // 시/도

    @Column(name = "city", length = 50)
    private String city;          // 시/군/구

    @Column(name = "district", length = 50)
    private String district;      // 동/읍/면

    @Column(name = "detail_address", length = 200)
    private String detailAddress; // 상세주소

    // ==================== 도메인 ↔ VO 변환 ====================

    /**
     * 도메인 모델을 VO로 변환
     */
    public static AddressVO fromDomain(Address domain) {
        if (domain == null) {
            return null;
        }
        return AddressVO.builder()
                .province(domain.getProvince())
                .city(domain.getCity())
                .district(domain.getDistrict())
                .detailAddress(domain.getDetailAddress())
                .build();
    }

    /**
     * VO를 도메인 모델로 변환
     */
    public Address toDomain() {
        return Address.builder()
                .province(this.province)
                .city(this.city)
                .district(this.district)
                .detailAddress(this.detailAddress)
                .build();
    }
}