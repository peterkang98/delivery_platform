package xyz.sparta_project.manjok.domain.favorites.presentation.rest.customer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.domain.favorites.application.service.FavoriteCommandService;
import xyz.sparta_project.manjok.domain.favorites.application.service.FavoriteQueryService;
import xyz.sparta_project.manjok.domain.favorites.domain.model.Favorite;
import xyz.sparta_project.manjok.domain.favorites.domain.model.FavoriteType;
import xyz.sparta_project.manjok.domain.favorites.presentation.rest.dto.FavoriteCheckResponse;
import xyz.sparta_project.manjok.domain.favorites.presentation.rest.dto.FavoriteCreateRequest;
import xyz.sparta_project.manjok.domain.favorites.presentation.rest.dto.FavoriteResponse;
import xyz.sparta_project.manjok.domain.favorites.presentation.rest.dto.FavoriteStatisticsResponse;
import xyz.sparta_project.manjok.global.infrastructure.security.SecurityUtils;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer용 Favorite API 컨트롤러
 * - 기본 경로: /v1/customers/favorites
 * - 권한: CUSTOMER
 */
@Slf4j
@RestController
@RequestMapping("/v1/customers/favorites")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class FavoriteCustomerController {

    private final FavoriteCommandService favoriteCommandService;
    private final FavoriteQueryService favoriteQueryService;

    /**
     * 찜하기 추가
     * POST /v1/customers/favorites
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FavoriteResponse> createFavorite(
            @Valid @RequestBody FavoriteCreateRequest request) {

        String customerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

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
     * DELETE /v1/customers/favorites/{favoriteId}
     */
    @DeleteMapping("/{favoriteId}")
    public ApiResponse<Void> deleteFavorite(
            @PathVariable String favoriteId) {

        String customerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("찜하기 삭제 요청 - CustomerId: {}, FavoriteId: {}", customerId, favoriteId);

        favoriteCommandService.removeFavorite(customerId, favoriteId);

        return ApiResponse.success(null, "찜하기가 취소되었습니다.");
    }

    /**
     * 내 찜하기 목록 조회 (전체)
     * GET /v1/customers/favorites
     */
    @GetMapping
    public ApiResponse<List<FavoriteResponse>> getMyFavorites() {

        String customerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("내 찜하기 목록 조회 - CustomerId: {}", customerId);

        List<Favorite> favorites = favoriteQueryService.getCustomerFavorites(customerId);
        List<FavoriteResponse> response = favorites.stream()
                .map(FavoriteResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(response);
    }

    /**
     * 내 찜하기 목록 조회 (타입별)
     * GET /v1/customers/favorites/type/{type}
     */
    @GetMapping("/type/{type}")
    public ApiResponse<List<FavoriteResponse>> getMyFavoritesByType(
            @PathVariable FavoriteType type) {

        String customerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("내 찜하기 목록 조회 (타입별) - CustomerId: {}, Type: {}", customerId, type);

        List<Favorite> favorites = favoriteQueryService.getCustomerFavoritesByType(customerId, type);
        List<FavoriteResponse> response = favorites.stream()
                .map(FavoriteResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(response);
    }

    /**
     * 찜하기 여부 확인 (레스토랑)
     * GET /v1/customers/favorites/check/restaurant/{restaurantId}
     */
    @GetMapping("/check/restaurant/{restaurantId}")
    public ApiResponse<FavoriteCheckResponse> checkRestaurantFavorite(
            @PathVariable String restaurantId) {

        String customerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("레스토랑 찜하기 여부 확인 - CustomerId: {}, RestaurantId: {}", customerId, restaurantId);

        boolean isFavorite = favoriteQueryService.isRestaurantFavorite(customerId, restaurantId);

        return ApiResponse.success(FavoriteCheckResponse.of(isFavorite));
    }

    /**
     * 찜하기 여부 확인 (메뉴)
     * GET /v1/customers/favorites/check/menu
     */
    @GetMapping("/check/menu")
    public ApiResponse<FavoriteCheckResponse> checkMenuFavorite(
            @RequestParam String restaurantId,
            @RequestParam String menuId) {

        String customerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("메뉴 찜하기 여부 확인 - CustomerId: {}, RestaurantId: {}, MenuId: {}",
                customerId, restaurantId, menuId);

        boolean isFavorite = favoriteQueryService.isMenuFavorite(customerId, restaurantId, menuId);

        return ApiResponse.success(FavoriteCheckResponse.of(isFavorite));
    }

    /**
     * 찜하기 상세 조회
     * GET /v1/customers/favorites/{favoriteId}
     */
    @GetMapping("/{favoriteId}")
    public ApiResponse<FavoriteResponse> getFavorite(
            @PathVariable String favoriteId) {

        String customerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("찜하기 상세 조회 - CustomerId: {}, FavoriteId: {}", customerId, favoriteId);

        Favorite favorite = favoriteQueryService.getFavorite(favoriteId);

        // 본인의 찜하기인지 검증
        if (!favorite.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }

        return ApiResponse.success(FavoriteResponse.from(favorite));
    }

    /**
     * 내 찜하기 통계 조회
     * GET /v1/customers/favorites/statistics
     */
    @GetMapping("/statistics")
    public ApiResponse<FavoriteStatisticsResponse> getMyFavoriteStatistics() {

        String customerId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다."));

        log.info("내 찜하기 통계 조회 - CustomerId: {}", customerId);

        FavoriteQueryService.FavoriteStatistics statistics =
                favoriteQueryService.getCustomerFavoriteStatistics(customerId);

        return ApiResponse.success(FavoriteStatisticsResponse.from(statistics));
    }
}