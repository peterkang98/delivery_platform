package xyz.sparta_project.manjok.domain.order.presentation.rest.customer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 주문 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @Valid
    @NotNull(message = "주문자 정보는 필수입니다.")
    private OrdererRequest orderer;

    @Valid
    @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다.")
    private List<OrderItemRequest> items;

    @NotBlank(message = "결제 ID는 필수입니다.")
    private String paymentKey;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrdererRequest {

        @NotBlank(message = "사용자 ID는 필수입니다.")
        private String userId;

        @NotBlank(message = "이름은 필수입니다.")
        private String name;

        @NotBlank(message = "연락처는 필수입니다.")
        private String phone;

        private String deliveryRequest;

        @Valid
        @NotNull(message = "배달 주소는 필수입니다.")
        private AddressRequest address;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressRequest {

        @NotBlank(message = "시/도는 필수입니다.")
        private String province;

        @NotBlank(message = "시/군/구는 필수입니다.")
        private String city;

        @NotBlank(message = "동/읍/면은 필수입니다.")
        private String district;

        @NotBlank(message = "상세 주소는 필수입니다.")
        private String detailAddress;

        @Valid
        @NotNull(message = "좌표는 필수입니다.")
        private CoordinateRequest coordinate;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CoordinateRequest {

        @NotNull(message = "위도는 필수입니다.")
        private BigDecimal latitude;

        @NotNull(message = "경도는 필수입니다.")
        private BigDecimal longitude;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequest {

        @NotBlank(message = "메뉴 ID는 필수입니다.")
        private String menuId;

        @NotBlank(message = "메뉴 이름은 필수입니다.")
        private String menuName;

        @NotNull(message = "기본 가격은 필수입니다.")
        @Positive(message = "기본 가격은 0보다 커야 합니다.")
        private BigDecimal basePrice;

        @NotNull(message = "수량은 필수입니다.")
        @Positive(message = "수량은 1개 이상이어야 합니다.")
        private Integer quantity;

        @Valid
        @NotNull(message = "레스토랑 정보는 필수입니다.")
        private RestaurantRequest restaurant;

        private List<OrderOptionGroupRequest> optionGroups;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RestaurantRequest {

        @NotBlank(message = "레스토랑 ID는 필수입니다.")
        private String restaurantId;

        @NotBlank(message = "레스토랑 이름은 필수입니다.")
        private String restaurantName;

        @NotBlank(message = "레스토랑 연락처는 필수입니다.")
        private String phone;

        @Valid
        @NotNull(message = "레스토랑 주소는 필수입니다.")
        private AddressRequest address;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderOptionGroupRequest {

        @NotBlank(message = "옵션 그룹명은 필수입니다.")
        private String groupName;

        @NotEmpty(message = "옵션은 최소 1개 이상이어야 합니다.")
        private List<OrderOptionRequest> options;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderOptionRequest {

        @NotBlank(message = "옵션명은 필수입니다.")
        private String optionName;

        private String description;

        @NotNull(message = "추가 가격은 필수입니다.")
        private BigDecimal additionalPrice;

        @NotNull(message = "수량은 필수입니다.")
        @Positive(message = "수량은 1개 이상이어야 합니다.")
        private Integer quantity;
    }
}