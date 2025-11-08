package xyz.sparta_project.manjok.domain.order.presentation.rest.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.domain.order.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private String orderId;
    private OrdererResponse orderer;
    private List<OrderItemResponse> items;
    private OrderStatus status;
    private PaymentResponse payment;
    private BigDecimal totalPrice;

    private LocalDateTime requestedAt;
    private LocalDateTime paymentCompletedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;
    private LocalDateTime canceledAt;
    private String cancelReason;

    private LocalDateTime createdAt;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrdererResponse {
        private String userId;
        private String name;
        private String phone;
        private String deliveryRequest;
        private AddressResponse address;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressResponse {
        private String province;
        private String city;
        private String district;
        private String detailAddress;
        private String fullAddress;
        private CoordinateResponse coordinate;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CoordinateResponse {
        private BigDecimal latitude;
        private BigDecimal longitude;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse {
        private String orderItemNumber;
        private String menuId;
        private String menuName;
        private BigDecimal basePrice;
        private Integer quantity;
        private BigDecimal totalPrice;
        private RestaurantResponse restaurant;
        private List<OrderOptionGroupResponse> optionGroups;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RestaurantResponse {
        private String restaurantId;
        private String restaurantName;
        private String phone;
        private AddressResponse address;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderOptionGroupResponse {
        private String groupName;
        private BigDecimal groupTotalPrice;
        private List<OrderOptionResponse> options;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderOptionResponse {
        private String optionName;
        private String description;
        private BigDecimal additionalPrice;
        private Integer quantity;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentResponse {
        private String paymentId;
        private Boolean isPaid;
    }
}