package xyz.sparta_project.manjok.domain.order.domain.model;

import lombok.*;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderErrorCode;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

/**
 * Orderer (Value Object)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"userId"})
public class Orderer {

    private String userId;
    private String name;
    private String phone;
    private Address address;
    private String deliveryRequest;

    /**
     * 팩토리 메서드 - 주문자 생성
     */
    public static Orderer create(String userId, String name, String phone,
                                 Address address, String deliveryRequest) {
        validateOrderer(userId, name, phone, address);

        return Orderer.builder()
                .userId(userId)
                .name(name)
                .phone(phone)
                .address(address)
                .deliveryRequest(deliveryRequest)
                .build();
    }

    /**
     * 주문자 정보 유효성 검증
     */
    private static void validateOrderer(String userId, String name, String phone, Address address) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new OrderException(OrderErrorCode.INVALID_ORDERER, "사용자 ID는 필수입니다.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new OrderException(OrderErrorCode.INVALID_ORDERER, "주문자 이름은 필수입니다.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new OrderException(OrderErrorCode.INVALID_ORDERER, "연락처는 필수입니다.");
        }
        if (address == null) {
            throw new OrderException(OrderErrorCode.INVALID_ADDRESS);
        }
    }
}