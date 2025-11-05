package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * MenuStatus Enum
 * - 메뉴 판매 상태
 */
@Getter
@RequiredArgsConstructor
public enum MenuStatus {

    AVAILABLE("판매중", "정상 판매 중"),
    SOLD_OUT("품절", "일시적으로 품절"),
    PREPARING("준비중", "판매 준비 중"),
    DISCONTINUED("판매종료", "판매 종료"),
    SEASONAL("계절메뉴", "특정 계절에만 판매");

    private final String displayName;
    private final String description;

    /**
     * 주문 가능한 상태인지 확인
     */
    public boolean isOrderable() {
        return this == AVAILABLE || this == SEASONAL;
    }
}
