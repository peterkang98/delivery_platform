package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Address;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Coordinate;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Restaurant;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.RestaurantStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RestaurantEntity 변환 테스트
 */
class RestaurantEntityTest {

    @Test
    @DisplayName("도메인 Restaurant를 RestaurantEntity로 변환")
    void fromDomain_ShouldConvertRestaurantToEntity() {
        // given
        Address address = Address.builder()
                .province("서울특별시")
                .city("강남구")
                .district("역삼동")
                .detailAddress("테헤란로 123")
                .build();

        Coordinate coordinate = Coordinate.builder()
                .latitude(new BigDecimal("37.5665350"))
                .longitude(new BigDecimal("126.9779690"))
                .build();

        Restaurant domain = Restaurant.builder()
                .id("REST123")
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .ownerId(1L)
                .ownerName("홍길동")
                .restaurantName("맛있는 식당")
                .status(RestaurantStatus.OPEN)
                .address(address)
                .coordinate(coordinate)
                .contactNumber("02-1234-5678")
                .tags(List.of("한식", "맛집"))
                .isActive(true)
                .viewCount(100)
                .createdBy("admin")
                .build();

        // when
        RestaurantEntity entity = RestaurantEntity.fromDomain(domain);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo("REST123");
        assertThat(entity.getOwnerId()).isEqualTo(1L);
        assertThat(entity.getOwnerName()).isEqualTo("홍길동");
        assertThat(entity.getRestaurantName()).isEqualTo("맛있는 식당");
        assertThat(entity.getStatus()).isEqualTo(RestaurantStatus.OPEN);
        assertThat(entity.getContactNumber()).isEqualTo("02-1234-5678");
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getViewCount()).isEqualTo(100);
        assertThat(entity.getCreatedBy()).isEqualTo("admin");

        // Address VO 검증
        assertThat(entity.getAddress()).isNotNull();
        assertThat(entity.getAddress().getProvince()).isEqualTo("서울특별시");
        assertThat(entity.getAddress().getCity()).isEqualTo("강남구");

        // Coordinate VO 검증
        assertThat(entity.getCoordinate()).isNotNull();
        assertThat(entity.getCoordinate().getLatitude()).isEqualByComparingTo(new BigDecimal("37.5665350"));
    }

    @Test
    @DisplayName("RestaurantEntity를 도메인 Restaurant로 변환")
    void toDomain_ShouldConvertEntityToRestaurant() {
        // given
        RestaurantEntity entity = RestaurantEntity.builder()
                .ownerId(2L)
                .ownerName("김철수")
                .restaurantName("즐거운 식당")
                .status(RestaurantStatus.CLOSED)
                .contactNumber("02-9876-5432")
                .isActive(false)
                .viewCount(50)
                .wishlistCount(20)
                .reviewCount(10)
                .reviewRating(new BigDecimal("4.5"))
                .purchaseCount(30)
                .createdBy("owner")
                .build();

        // when
        Restaurant domain = entity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getOwnerId()).isEqualTo(2L);
        assertThat(domain.getOwnerName()).isEqualTo("김철수");
        assertThat(domain.getRestaurantName()).isEqualTo("즐거운 식당");
        assertThat(domain.getStatus()).isEqualTo(RestaurantStatus.CLOSED);
        assertThat(domain.getContactNumber()).isEqualTo("02-9876-5432");
        assertThat(domain.getIsActive()).isFalse();
        assertThat(domain.getViewCount()).isEqualTo(50);
        assertThat(domain.getWishlistCount()).isEqualTo(20);
        assertThat(domain.getReviewCount()).isEqualTo(10);
        assertThat(domain.getReviewRating()).isEqualByComparingTo(new BigDecimal("4.5"));
        assertThat(domain.getPurchaseCount()).isEqualTo(30);
        assertThat(domain.getCreatedBy()).isEqualTo("owner");
    }
}