package xyz.sparta_project.manjok.domain.order.domain.model;

import lombok.*;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderErrorCode;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

/**
 * Address (Value Object)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"province", "city", "district", "detailAddress"})
public class Address {

    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Coordinate coordinate;

    /**
     * 팩토리 메서드 - 주소 생성
     */
    public static Address create(String province, String city, String district,
                                 String detailAddress, Coordinate coordinate) {
        validateAddress(province, city, district, detailAddress, coordinate);

        return Address.builder()
                .province(province)
                .city(city)
                .district(district)
                .detailAddress(detailAddress)
                .coordinate(coordinate)
                .build();
    }

    /**
     * 주소 유효성 검증
     */
    private static void validateAddress(String province, String city, String district,
                                        String detailAddress, Coordinate coordinate) {
        if (province == null || province.trim().isEmpty()) {
            throw new OrderException(OrderErrorCode.INVALID_ADDRESS, "시/도는 필수입니다.");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new OrderException(OrderErrorCode.INVALID_ADDRESS, "시/군/구는 필수입니다.");
        }
        if (district == null || district.trim().isEmpty()) {
            throw new OrderException(OrderErrorCode.INVALID_ADDRESS, "동/읍/면은 필수입니다.");
        }
        if (detailAddress == null || detailAddress.trim().isEmpty()) {
            throw new OrderException(OrderErrorCode.INVALID_ADDRESS, "상세 주소는 필수입니다.");
        }
        if (coordinate == null) {
            throw new OrderException(OrderErrorCode.INVALID_COORDINATE);
        }
    }

    /**
     * 전체 주소 문자열 반환
     */
    public String getFullAddress() {
        return String.format("%s %s %s %s", province, city, district, detailAddress);
    }
}