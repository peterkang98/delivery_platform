// FavoriteType.java
package xyz.sparta_project.manjok.domain.favorites.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 찜하기 타입
 */
@Getter
@RequiredArgsConstructor
public enum FavoriteType {
    RESTAURANT("레스토랑", "레스토랑 찜하기"),
    MENU("메뉴", "메뉴 찜하기");

    private final String displayName;
    private final String description;

    /**
     * 문자열로부터 FavoriteType 찾기
     */
    public static FavoriteType fromString(String value) {
        for (FavoriteType type : FavoriteType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid FavoriteType: " + value);
    }

    /**
     * 레스토랑 타입인지 확인
     */
    public boolean isRestaurant() {
        return this == RESTAURANT;
    }

    /**
     * 메뉴 타입인지 확인
     */
    public boolean isMenu() {
        return this == MENU;
    }
}