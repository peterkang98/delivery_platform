package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Owner용 Restaurant 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantCreateRequest {

    @NotBlank(message = "식당명은 필수입니다.")
    private String restaurantName;

    @NotNull(message = "주소 정보는 필수입니다.")
    @Valid
    private AddressDto address;

    @NotBlank(message = "연락처는 필수입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다. (예: 02-1234-5678)")
    private String contactNumber;

    private CoordinateDto coordinate;

    private List<String> tags;

    private Set<String> categoryIds;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDto {
        @NotBlank(message = "시/도는 필수입니다.")
        private String province;

        @NotBlank(message = "시/군/구는 필수입니다.")
        private String city;

        @NotBlank(message = "동/읍/면은 필수입니다.")
        private String district;

        private String detailAddress;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinateDto {
        @NotNull(message = "위도는 필수입니다.")
        private BigDecimal latitude;

        @NotNull(message = "경도는 필수입니다.")
        private BigDecimal longitude;
    }
}