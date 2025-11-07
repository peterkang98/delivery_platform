// FavoriteController.java
package xyz.sparta_project.manjok.domain.favorites.presentation.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.favorites.application.service.FavoriteCommandService;
import xyz.sparta_project.manjok.domain.favorites.application.service.FavoriteQueryService;
import xyz.sparta_project.manjok.domain.favorites.domain.model.Favorite;
import xyz.sparta_project.manjok.domain.favorites.domain.model.FavoriteType;
import xyz.sparta_project.manjok.domain.favorites.presentation.rest.dto.FavoriteCheckResponse;
import xyz.sparta_project.manjok.domain.favorites.presentation.rest.dto.FavoriteCreateRequest;
import xyz.sparta_project.manjok.domain.favorites.presentation.rest.dto.FavoriteResponse;
import xyz.sparta_project.manjok.domain.favorites.presentation.rest.dto.FavoriteStatisticsResponse;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteCommandService favoriteCommandService;
    private final FavoriteQueryService favoriteQueryService;

    /**
     * 찜하기 추가
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FavoriteResponse> createFavorite(
            @RequestHeader("X-Customer-Id") String customerId,
            @Valid @RequestBody FavoriteCreateRequest request
    ) {
        log.info("찜하기 추가 요청 - CustomerId: {}, Type: {}, RestaurantId: {}, MenuId: {}",
                customerId, request.getType(), request.getRestaurantId(), request.getMenuId());

        Favorite favorite;

        if (request.getType() == FavoriteType.RESTAURANT) {
            favorite = favoriteCommandService.addRestaurantFavorite(
                    customerId,
                    request.getRestaurantId()
            );
        } else {
            favorite = favoriteCommandService.addMenuFavorite(
                    customerId,
                    request.getRestaurantId(),
                    request.getMenuId()
            );
        }

        return ApiResponse.success(
                FavoriteResponse.from(favorite),
                "찜하기가 추가되었습니다."
        );
    }

    /**
     * 찜하기 삭제
     */
    @DeleteMapping("/{favoriteId}")
    public ApiResponse<Void> deleteFavorite(
            @RequestHeader("X-Customer-Id") String customerId,
            @PathVariable String favoriteId
    ) {
        log.info("찜하기 삭제 요청 - CustomerId: {}, FavoriteId: {}", customerId, favoriteId);

        favoriteCommandService.removeFavorite(customerId, favoriteId);

        return ApiResponse.success(null, "찜하기가 취소되었습니다.");
    }

    /**
     * 내 찜하기 목록 조회 (전체)
     */
    @GetMapping
    public ApiResponse<List<FavoriteResponse>> getMyFavorites(
            @RequestHeader("X-Customer-Id") String customerId
    ) {
        log.info("내 찜하기 목록 조회 - CustomerId: {}", customerId);

        List<Favorite> favorites = favoriteQueryService.getCustomerFavorites(customerId);
        List<FavoriteResponse> response = favorites.stream()
                .map(FavoriteResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(response);
    }

    /**
     * 내 찜하기 목록 조회 (타입별)
     */
    @GetMapping("/type/{type}")
    public ApiResponse<List<FavoriteResponse>> getMyFavoritesByType(
            @RequestHeader("X-Customer-Id") String customerId,
            @PathVariable FavoriteType type
    ) {
        log.info("내 찜하기 목록 조회 (타입별) - CustomerId: {}, Type: {}", customerId, type);

        List<Favorite> favorites = favoriteQueryService.getCustomerFavoritesByType(customerId, type);
        List<FavoriteResponse> response = favorites.stream()
                .map(FavoriteResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(response);
    }

    /**
     * 찜하기 여부 확인 (레스토랑)
     */
    @GetMapping("/check/restaurant/{restaurantId}")
    public ApiResponse<FavoriteCheckResponse> checkRestaurantFavorite(
            @RequestHeader("X-Customer-Id") String customerId,
            @PathVariable String restaurantId
    ) {
        log.info("레스토랑 찜하기 여부 확인 - CustomerId: {}, RestaurantId: {}", customerId, restaurantId);

        boolean isFavorite = favoriteQueryService.isRestaurantFavorite(customerId, restaurantId);

        return ApiResponse.success(FavoriteCheckResponse.of(isFavorite));
    }

    /**
     * 찜하기 여부 확인 (메뉴)
     */
    @GetMapping("/check/menu")
    public ApiResponse<FavoriteCheckResponse> checkMenuFavorite(
            @RequestHeader("X-Customer-Id") String customerId,
            @RequestParam String restaurantId,
            @RequestParam String menuId
    ) {
        log.info("메뉴 찜하기 여부 확인 - CustomerId: {}, RestaurantId: {}, MenuId: {}",
                customerId, restaurantId, menuId);

        boolean isFavorite = favoriteQueryService.isMenuFavorite(customerId, restaurantId, menuId);

        return ApiResponse.success(FavoriteCheckResponse.of(isFavorite));
    }

    /**
     * 찜하기 상세 조회
     */
    @GetMapping("/{favoriteId}")
    public ApiResponse<FavoriteResponse> getFavorite(
            @PathVariable String favoriteId
    ) {
        log.info("찜하기 상세 조회 - FavoriteId: {}", favoriteId);

        Favorite favorite = favoriteQueryService.getFavorite(favoriteId);

        return ApiResponse.success(FavoriteResponse.from(favorite));
    }

    /**
     * 내 찜하기 통계 조회
     */
    @GetMapping("/statistics")
    public ApiResponse<FavoriteStatisticsResponse> getMyFavoriteStatistics(
            @RequestHeader("X-Customer-Id") String customerId
    ) {
        log.info("내 찜하기 통계 조회 - CustomerId: {}", customerId);

        FavoriteQueryService.FavoriteStatistics statistics =
                favoriteQueryService.getCustomerFavoriteStatistics(customerId);

        return ApiResponse.success(FavoriteStatisticsResponse.from(statistics));
    }

    /**
     * 레스토랑의 찜하기 개수 조회
     */
    @GetMapping("/restaurant/{restaurantId}/count")
    public ApiResponse<Long> getRestaurantFavoriteCount(
            @PathVariable String restaurantId
    ) {
        log.info("레스토랑 찜하기 개수 조회 - RestaurantId: {}", restaurantId);

        long count = favoriteQueryService.getRestaurantFavoriteCount(restaurantId);

        return ApiResponse.success(count);
    }
}