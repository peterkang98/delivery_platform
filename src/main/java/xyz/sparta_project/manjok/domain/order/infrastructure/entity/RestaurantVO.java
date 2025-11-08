package xyz.sparta_project.manjok.domain.order.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import xyz.sparta_project.manjok.domain.order.domain.model.Address;
import xyz.sparta_project.manjok.domain.order.domain.model.Coordinate;
import xyz.sparta_project.manjok.domain.order.domain.model.Restaurant;

import java.math.BigDecimal;

/**
 * Restaurant Value Object (스냅샷)
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RestaurantVO {

    @Column(name = "restaurant_id", nullable = false, length = 36)
    private String restaurantId;

    @Column(name = "restaurant_name", nullable = false, length = 200)
    private String restaurantName;

    @Column(name = "restaurant_phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "restaurant_province", nullable = false, length = 50)
    private String province;

    @Column(name = "restaurant_city", nullable = false, length = 50)
    private String city;

    @Column(name = "restaurant_district", nullable = false, length = 50)
    private String district;

    @Column(name = "restaurant_detail_address", nullable = false, length = 200)
    private String detailAddress;

    @Column(name = "restaurant_latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "restaurant_longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    /**
     * 도메인 → VO
     */
    public static RestaurantVO from(Restaurant domain) {
        Address address = domain.getAddress();
        Coordinate coordinate = address.getCoordinate();

        return RestaurantVO.builder()
                .restaurantId(domain.getRestaurantId())
                .restaurantName(domain.getRestaurantName())
                .phone(domain.getPhone())
                .province(address.getProvince())
                .city(address.getCity())
                .district(address.getDistrict())
                .detailAddress(address.getDetailAddress())
                .latitude(coordinate.getLatitude())
                .longitude(coordinate.getLongitude())
                .build();
    }

    /**
     * VO → 도메인
     */
    public Restaurant toDomain() {
        Coordinate coordinate = Coordinate.builder()
                .latitude(this.latitude)
                .longitude(this.longitude)
                .build();

        Address address = Address.builder()
                .province(this.province)
                .city(this.city)
                .district(this.district)
                .detailAddress(this.detailAddress)
                .coordinate(coordinate)
                .build();

        return Restaurant.builder()
                .restaurantId(this.restaurantId)
                .restaurantName(this.restaurantName)
                .phone(this.phone)
                .address(address)
                .build();
    }
}