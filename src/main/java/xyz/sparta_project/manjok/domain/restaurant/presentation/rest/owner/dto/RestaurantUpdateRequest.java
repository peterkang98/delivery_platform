package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Owner용 Restaurant 수정 요청 DTO
 * - PUT/PATCH 모두 사용 가능
 * - PATCH의 경우 null이 아닌 필드만 업데이트
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantUpdateRequest {

    private String restaurantName;

    private AddressDto address;

    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다. (예: 02-1234-5678)")
    private String contactNumber;

    private CoordinateDto coordinate;

    private List<String> tags;

    private Set<String> categoryIds;

    private String status; // OPEN, CLOSED, TEMPORARILY_CLOSED, PREPARING

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDto {
        private String province;
        private String city;
        private String district;
        private String detailAddress;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinateDto {
        private BigDecimal latitude;
        private BigDecimal longitude;
    }
}