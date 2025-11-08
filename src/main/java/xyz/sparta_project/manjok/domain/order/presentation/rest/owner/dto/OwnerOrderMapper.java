package xyz.sparta_project.manjok.domain.order.presentation.rest.owner;

import org.springframework.stereotype.Component;
import xyz.sparta_project.manjok.domain.order.domain.model.*;
import xyz.sparta_project.manjok.domain.order.presentation.rest.customer.dto.OrderResponse;

import java.util.stream.Collectors;

/**
 * Owner DTO ↔ 도메인 매퍼
 */
@Component
public class OwnerOrderMapper {

    public OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .orderer(toOrdererResponse(order.getOrderer()))
                .items(order.getItems().stream()
                        .map(this::toOrderItemResponse)
                        .collect(Collectors.toList()))
                .status(order.getStatus())
                .payment(toPaymentResponse(order.getPayment()))
                .totalPrice(order.getTotalPrice())
                .requestedAt(order.getRequestedAt())
                .paymentCompletedAt(order.getPaymentCompletedAt())
                .confirmedAt(order.getConfirmedAt())
                .completedAt(order.getCompletedAt())
                .canceledAt(order.getCanceledAt())
                .cancelReason(order.getCancelReason())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderResponse.OrdererResponse toOrdererResponse(Orderer orderer) {
        return OrderResponse.OrdererResponse.builder()
                .userId(orderer.getUserId())
                .name(orderer.getName())
                .phone(orderer.getPhone())
                .deliveryRequest(orderer.getDeliveryRequest())
                .address(toAddressResponse(orderer.getAddress()))
                .build();
    }

    private OrderResponse.AddressResponse toAddressResponse(Address address) {
        return OrderResponse.AddressResponse.builder()
                .province(address.getProvince())
                .city(address.getCity())
                .district(address.getDistrict())
                .detailAddress(address.getDetailAddress())
                .fullAddress(address.getFullAddress())
                .coordinate(toCoordinateResponse(address.getCoordinate()))
                .build();
    }

    private OrderResponse.CoordinateResponse toCoordinateResponse(Coordinate coordinate) {
        return OrderResponse.CoordinateResponse.builder()
                .latitude(coordinate.getLatitude())
                .longitude(coordinate.getLongitude())
                .build();
    }

    private OrderResponse.OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderResponse.OrderItemResponse.builder()
                .orderItemNumber(item.getOrderItemNumber())
                .menuId(item.getMenuId())
                .menuName(item.getMenuName())
                .basePrice(item.getBasePrice())
                .quantity(item.getQuantity())
                .totalPrice(item.getTotalPrice())
                .restaurant(toRestaurantResponse(item.getRestaurant()))
                .optionGroups(item.getOptionGroups().stream()
                        .map(this::toOrderOptionGroupResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private OrderResponse.RestaurantResponse toRestaurantResponse(Restaurant restaurant) {
        return OrderResponse.RestaurantResponse.builder()
                .restaurantId(restaurant.getRestaurantId())
                .restaurantName(restaurant.getRestaurantName())
                .phone(restaurant.getPhone())
                .address(toAddressResponse(restaurant.getAddress()))
                .build();
    }

    private OrderResponse.OrderOptionGroupResponse toOrderOptionGroupResponse(OrderOptionGroup group) {
        return OrderResponse.OrderOptionGroupResponse.builder()
                .groupName(group.getGroupName())
                .groupTotalPrice(group.getGroupTotalPrice())
                .options(group.getOptions().stream()
                        .map(this::toOrderOptionResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private OrderResponse.OrderOptionResponse toOrderOptionResponse(OrderOption option) {
        return OrderResponse.OrderOptionResponse.builder()
                .optionName(option.getOptionName())
                .description(option.getDescription())
                .additionalPrice(option.getAdditionalPrice())
                .quantity(option.getQuantity())
                .build();
    }

    private OrderResponse.PaymentResponse toPaymentResponse(Payment payment) {
        return OrderResponse.PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .isPaid(payment.getIsPaid())
                .build();
    }
}