package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

/**
 * Address Value Object
 * - 주소 정보를 표현하는 불변 객체
 * - 순수 도메인 모델
 * */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class Address {

    private String province;       // 시/도 (예: 서울특별시)
    private String city;          // 시/군/구 (예: 종로구)
    private String district;      // 동/읍/면 (예: 광화문동)
    private String detailAddress; // 상세주소 (예: 123-4번지 2층)

    /**
     * 전체 주소 조합
     * */
    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        fullAddress.append(province).append(" ")
                .append(city).append(" ")
                .append(district);
        if (detailAddress != null && !detailAddress.isEmpty()) {
            fullAddress.append(" ").append(detailAddress);
        }

        return fullAddress.toString();
    }

    /**
     * 지역 핉터링용 주소 (시/구/동)
     * */
    public String getAreaAddress() {
        return String.format("%s %s %s", province, city, district);
    }

    /**
     * 주소 유효성 검증
     * */
    public boolean isValid() {
        return province != null && !province.isEmpty() &&
               city != null && !city.isEmpty() &&
               district != null && !district.isEmpty();
    }
}
