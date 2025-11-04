package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.RestaurantCategory;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RestaurantCategoryEntity 변환 테스트
 */
class RestaurantCategoryEntityTest {

    @Test
    @DisplayName("도메인 RestaurantCategory를 RestaurantCategoryEntity로 변환")
    void fromDomain_ShouldConvertRestaurantCategoryToEntity() {
        // given
        RestaurantCategory domain = RestaurantCategory.builder()
                .id("CAT123")
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .categoryCode("KOREAN")
                .categoryName("한식")
                .description("전통 한식 음식점")
                .iconUrl("/icons/korean.png")
                .colorCode("#FF5733")
                .depth(1)
                .displayOrder(1)
                .isActive(true)
                .isPopular(true)
                .defaultMinimumOrderAmount(15000)
                .averageDeliveryTime(30)
                .platformCommissionRate(12.5)
                .activeRestaurantCount(100)
                .totalOrderCount(5000)
                .createdBy("admin")
                .build();

        // when
        RestaurantCategoryEntity entity = RestaurantCategoryEntity.fromDomain(domain);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo("CAT123");
        assertThat(entity.getCategoryCode()).isEqualTo("KOREAN");
        assertThat(entity.getCategoryName()).isEqualTo("한식");
        assertThat(entity.getDescription()).isEqualTo("전통 한식 음식점");
        assertThat(entity.getIconUrl()).isEqualTo("/icons/korean.png");
        assertThat(entity.getColorCode()).isEqualTo("#FF5733");
        assertThat(entity.getDepth()).isEqualTo(1);
        assertThat(entity.getDisplayOrder()).isEqualTo(1);
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getIsPopular()).isTrue();
        assertThat(entity.getDefaultMinimumOrderAmount()).isEqualTo(15000);
        assertThat(entity.getAverageDeliveryTime()).isEqualTo(30);
        assertThat(entity.getPlatformCommissionRate()).isEqualTo(12.5);
        assertThat(entity.getActiveRestaurantCount()).isEqualTo(100);
        assertThat(entity.getTotalOrderCount()).isEqualTo(5000);
        assertThat(entity.getCreatedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("RestaurantCategoryEntity를 도메인 RestaurantCategory로 변환")
    void toDomain_ShouldConvertEntityToRestaurantCategory() {
        // given
        RestaurantCategoryEntity entity = RestaurantCategoryEntity.builder()
                .categoryCode("CHINESE")
                .categoryName("중식")
                .description("중국 음식점")
                .iconUrl("/icons/chinese.png")
                .colorCode("#FFA500")
                .depth(1)
                .displayOrder(2)
                .isActive(true)
                .isPopular(false)
                .defaultMinimumOrderAmount(12000)
                .averageDeliveryTime(25)
                .platformCommissionRate(10.0)
                .activeRestaurantCount(80)
                .totalOrderCount(3000)
                .createdBy("system")
                .build();

        // when
        RestaurantCategory domain = entity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getCategoryCode()).isEqualTo("CHINESE");
        assertThat(domain.getCategoryName()).isEqualTo("중식");
        assertThat(domain.getDescription()).isEqualTo("중국 음식점");
        assertThat(domain.getIconUrl()).isEqualTo("/icons/chinese.png");
        assertThat(domain.getColorCode()).isEqualTo("#FFA500");
        assertThat(domain.getDepth()).isEqualTo(1);
        assertThat(domain.getDisplayOrder()).isEqualTo(2);
        assertThat(domain.getIsActive()).isTrue();
        assertThat(domain.getIsPopular()).isFalse();
        assertThat(domain.getDefaultMinimumOrderAmount()).isEqualTo(12000);
        assertThat(domain.getAverageDeliveryTime()).isEqualTo(25);
        assertThat(domain.getPlatformCommissionRate()).isEqualTo(10.0);
        assertThat(domain.getActiveRestaurantCount()).isEqualTo(80);
        assertThat(domain.getTotalOrderCount()).isEqualTo(3000);
        assertThat(domain.getCreatedBy()).isEqualTo("system");
    }
}