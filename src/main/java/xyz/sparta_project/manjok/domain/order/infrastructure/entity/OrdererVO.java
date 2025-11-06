package xyz.sparta_project.manjok.domain.order.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Embeddable;
import lombok.*;
import xyz.sparta_project.manjok.domain.order.domain.model.Orderer;

/**
 * Orderer Value Object
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OrdererVO {

    @Column(name = "orderer_user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "orderer_name", nullable = false, length = 100)
    private String name;

    @Column(name = "orderer_phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "delivery_request", length = 500)
    private String deliveryRequest;

    @Embedded
    private AddressVO address;

    /**
     * 도메인 → VO
     */
    public static OrdererVO from(Orderer domain) {
        return OrdererVO.builder()
                .userId(domain.getUserId())
                .name(domain.getName())
                .phone(domain.getPhone())
                .deliveryRequest(domain.getDeliveryRequest())
                .address(AddressVO.from(domain.getAddress()))
                .build();
    }

    /**
     * VO → 도메인
     */
    public Orderer toDomain() {
        return Orderer.builder()
                .userId(this.userId)
                .name(this.name)
                .phone(this.phone)
                .deliveryRequest(this.deliveryRequest)
                .address(this.address.toDomain())
                .build();
    }
}