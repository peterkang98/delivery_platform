// FavoriteCheckResponse.java
package xyz.sparta_project.manjok.domain.favorites.presentation.rest.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FavoriteCheckResponse {

    private boolean isFavorite;

    /**
     * boolean 값을 DTO로 변환
     */
    public static FavoriteCheckResponse of(boolean isFavorite) {
        return FavoriteCheckResponse.builder()
                .isFavorite(isFavorite)
                .build();
    }
}