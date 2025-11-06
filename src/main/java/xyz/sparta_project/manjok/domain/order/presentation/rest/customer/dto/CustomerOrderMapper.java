package xyz.sparta_project.manjok.domain.order.presentation.rest.customer;

import org.springframework.stereotype.Component;
import xyz.sparta_project.manjok.domain.order.domain.model.*;
import xyz.sparta_project.manjok.domain.order.presentation.rest.customer.dto.CreateOrderRequest;
import xyz.sparta_project.manjok.domain.order.presentation.rest.customer.dto.OrderResponse;

import java.util.stream.Collectors;

/**
 * Customer DTO ↔ 도메인 매퍼
 */
@Component
public class CustomerOrderMapper {

    // === Request → Domain ===

    public Orderer toOrderer(CreateOrderRequest.OrdererRequest request) {
        return Orderer.create(
                request.getUserId(),
                request.getName(),
                request.getPhone(),
                toAddress(request.getAddress()),
                request.getDeliveryRequest()
        );
    }

    public Address toAddress(CreateOrderRequest.AddressRequest request) {
        return Address.create(
                request.getProvince(),
                request.getCity(),
                request.getDistrict(),
                request.getDetailAddress(),
                toCoordinate(request.getCoordinate())
        );
    }

    public Coordinate toCoordinate(CreateOrderRequest.CoordinateRequest request) {
        return Coordinate.create(
                request.getLatitude(),
                request.getLongitude()
        );
    }

    public OrderItem toOrderItem(CreateOrderRequest.OrderItemRequest request) {
        return OrderItem.create(
                request.getMenuId(),
                request.getMenuName(),
                request.getBasePrice(),
                request.getQuantity(),
                toRestaurant(request.getRestaurant()),
                request.getOptionGroups() != null ?
                        request.getOptionGroups().stream()
                                .map(this::toOrderOptionGroup)
                                .collect(Collectors.toList())
                        : null
        );
    }

    public Restaurant toRestaurant(CreateOrderRequest.RestaurantRequest request) {
        return Restaurant.create(
                request.getRestaurantId(),
                request.getRestaurantName(),
                request.getPhone(),
                toAddress(request.getAddress())
        );
    }

    public OrderOptionGroup toOrderOptionGroup(CreateOrderRequest.OrderOptionGroupRequest request) {
        return OrderOptionGroup.create(
                request.getGroupName(),
                request.getOptions().stream()
                        .map(this::toOrderOption)
                        .collect(Collectors.toList())
        );
    }

    public OrderOption toOrderOption(CreateOrderRequest.OrderOptionRequest request) {
        return OrderOption.create(
                request.getOptionName(),
                request.getDescription(),
                request.getAdditionalPrice(),
                request.getQuantity()
        );
    }

    // === Domain → Response ===

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