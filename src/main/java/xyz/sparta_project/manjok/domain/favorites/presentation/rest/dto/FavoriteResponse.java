// FavoriteResponse.java
package xyz.sparta_project.manjok.domain.favorites.presentation.rest.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import xyz.sparta_project.manjok.domain.favorites.domain.model.Favorite;
import xyz.sparta_project.manjok.domain.favorites.domain.model.FavoriteType;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FavoriteResponse {

    private String id;
    private String customerId;
    private FavoriteType type;
    private String restaurantId;
    private String menuId;
    private LocalDateTime createdAt;

    /**
     * 도메인 모델을 DTO로 변환
     */
    public static FavoriteResponse from(Favorite favorite) {
        return FavoriteResponse.builder()
                .id(favorite.getId())
                .customerId(favorite.getCustomerId())
                .type(favorite.getType())
                .restaurantId(favorite.getRestaurantId())
                .menuId(favorite.getMenuId())
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}