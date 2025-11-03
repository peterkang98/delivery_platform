package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import lombok.Getter;

/**
 * 레스토랑 운영 상태
 * */
@Getter
public enum RestaurantStatus {

    OPEN("영업중", "영업 중입니다"),
    CLOSED("영업종료", "영업이 종료되었습니다"),
    TEMPORARILY_CLOSED("임시휴업", "임시 휴업 중입니다"),
    PREPARING("준비중", "영업 준비 중입니다");

    private final String displayName;
    private final String description;

    RestaurantStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * 주문 가능 상태인지 확인
     */
    public boolean canAcceptOrder() {
        return this == OPEN;
    }

    /**
     * 메뉴 수정 가능 상태인지 확인
     * - 영업 중에는 고객 혼란 방지를 위해 메뉴 수정 불가
     * - 영업 종료, 임시 휴업, 준비 중에는 메뉴 수정 가능
     */
    public boolean canModifyMenu() {
        return this != OPEN;
    }
}
