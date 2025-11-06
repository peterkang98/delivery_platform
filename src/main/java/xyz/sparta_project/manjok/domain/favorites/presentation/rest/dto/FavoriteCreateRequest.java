// FavoriteCreateRequest.java
package xyz.sparta_project.manjok.domain.favorites.presentation.rest.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.domain.favorites.domain.model.FavoriteType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FavoriteCreateRequest {

    @NotNull(message = "찜하기 타입은 필수입니다.")
    private FavoriteType type;

    @NotBlank(message = "레스토랑 ID는 필수입니다.")
    private String restaurantId;

    private String menuId; // 메뉴 타입일 때만 필수
}