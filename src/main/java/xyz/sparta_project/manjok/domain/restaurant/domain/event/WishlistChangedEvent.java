package xyz.sparta_project.manjok.domain.restaurant.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 찜 추가/제거 이벤트
 */
@Getter
@RequiredArgsConstructor
public class WishlistChangedEvent {
    private final String restaurantId;
    private final String menuId;  // nullable (레스토랑 찜인 경우)
    private final WishlistAction action;

    public enum WishlistAction {
        ADDED, REMOVED
    }
}