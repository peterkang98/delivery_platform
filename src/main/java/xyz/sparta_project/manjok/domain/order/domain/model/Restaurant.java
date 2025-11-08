package xyz.sparta_project.manjok.domain.order.domain.model;

import lombok.*;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderErrorCode;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

/**
 * Restaurant (Value Object)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"restaurantId"})
public class Restaurant {

    private String restaurantId;
    private String restaurantName;
    private String phone;
    private Address address;

    /**
     * 팩토리 메서드 - 레스토랑 정보 생성
     */
    public static Restaurant create(String restaurantId, String restaurantName,
                                    String phone, Address address) {
        validateRestaurant(restaurantId, restaurantName, phone, address);

        return Restaurant.builder()
                .restaurantId(restaurantId)
                .restaurantName(restaurantName)
                .phone(phone)
                .address(address)
                .build();
    }

    /**
     * 레스토랑 정보 유효성 검증
     */
    private static void validateRestaurant(String restaurantId, String restaurantName,
                                           String phone, Address address) {
        if (restaurantId == null || restaurantId.trim().isEmpty()) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_ITEMS, "레스토랑 ID는 필수입니다.");
        }
        if (restaurantName == null || restaurantName.trim().isEmpty()) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_ITEMS, "레스토랑 이름은 필수입니다.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_ITEMS, "레스토랑 연락처는 필수입니다.");
        }
        if (address == null) {
            throw new OrderException(OrderErrorCode.INVALID_ADDRESS);
        }
    }
}