// FavoriteStatisticsResponse.java
package xyz.sparta_project.manjok.domain.favorites.presentation.rest.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import xyz.sparta_project.manjok.domain.favorites.application.service.FavoriteQueryService.FavoriteStatistics;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FavoriteStatisticsResponse {

    private long totalCount;
    private long restaurantCount;
    private long menuCount;

    /**
     * 통계 객체를 DTO로 변환
     */
    public static FavoriteStatisticsResponse from(FavoriteStatistics statistics) {
        return FavoriteStatisticsResponse.builder()
                .totalCount(statistics.getTotalCount())
                .restaurantCount(statistics.getRestaurantCount())
                .menuCount(statistics.getMenuCount())
                .build();
    }
}