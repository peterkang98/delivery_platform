package xyz.sparta_project.manjok.domain.favorites.presentation.rest.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.favorites.application.service.FavoriteQueryService;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;

/**
 * Favorite 공통 API 컨트롤러 (인증 불필요)
 * - 기본 경로: /v1/common/favorites
 */
@Slf4j
@RestController
@RequestMapping("/v1/common/favorites")
@RequiredArgsConstructor
public class FavoriteCommonController {

    private final FavoriteQueryService favoriteQueryService;

    /**
     * 레스토랑의 찜하기 개수 조회
     * GET /v1/common/favorites/restaurant/{restaurantId}/count
     */
    @GetMapping("/restaurant/{restaurantId}/count")
    public ApiResponse<Long> getRestaurantFavoriteCount(
            @PathVariable String restaurantId) {

        log.info("레스토랑 찜하기 개수 조회 - RestaurantId: {}", restaurantId);

        long count = favoriteQueryService.getRestaurantFavoriteCount(restaurantId);

        return ApiResponse.success(count);
    }
}