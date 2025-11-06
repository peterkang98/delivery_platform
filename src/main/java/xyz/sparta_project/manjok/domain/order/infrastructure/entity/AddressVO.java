package xyz.sparta_project.manjok.domain.order.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Embeddable;
import lombok.*;
import xyz.sparta_project.manjok.domain.order.domain.model.Address;

/**
 * Address Value Object
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AddressVO {

    @Column(name = "orderer_province", nullable = false, length = 50)
    private String province;

    @Column(name = "orderer_city", nullable = false, length = 50)
    private String city;

    @Column(name = "orderer_district", nullable = false, length = 50)
    private String district;

    @Column(name = "orderer_detail_address", nullable = false, length = 200)
    private String detailAddress;

    @Embedded
    private CoordinateVO coordinate;

    /**
     * 도메인 → VO
     */
    public static AddressVO from(Address domain) {
        return AddressVO.builder()
                .province(domain.getProvince())
                .city(domain.getCity())
                .district(domain.getDistrict())
                .detailAddress(domain.getDetailAddress())
                .coordinate(CoordinateVO.from(domain.getCoordinate()))
                .build();
    }

    /**
     * VO → 도메인
     */
    public Address toDomain() {
        return Address.builder()
                .province(this.province)
                .city(this.city)
                .district(this.district)
                .detailAddress(this.detailAddress)
                .coordinate(this.coordinate.toDomain())
                .build();
    }
}