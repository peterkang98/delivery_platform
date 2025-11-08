package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Menu;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MenuEntity 변환 테스트 (연관관계 매핑 적용)
 */
class MenuEntityTest {

    @Test
    @DisplayName("도메인 Menu를 MenuEntity로 변환")
    void fromDomain_ShouldConvertMenuToEntity() {
        // given
        Menu domain = Menu.builder()
                .id("menu-id-123")
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .restaurantId("restaurant-456")
                .menuName("불고기 정식")
                .description("맛있는 불고기")
                .ingredients("소고기, 야채")
                .price(new BigDecimal("15000"))
                .isAvailable(true)
                .isMain(true)
                .isPopular(true)
                .calorie(650)
                .purchaseCount(100)
                .wishlistCount(50)
                .reviewCount(20)
                .reviewRating(new BigDecimal("4.5"))
                .createdBy("owner")
                .build();

        // when
        MenuEntity entity = MenuEntity.fromDomain(domain);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo("menu-id-123");
        // Restaurant 연관관계는 null (Restaurant에서 설정해야 함)
        assertThat(entity.getRestaurant()).isNull();
        assertThat(entity.getMenuName()).isEqualTo("불고기 정식");
        assertThat(entity.getDescription()).isEqualTo("맛있는 불고기");
        assertThat(entity.getIngredients()).isEqualTo("소고기, 야채");
        assertThat(entity.getPrice()).isEqualByComparingTo(new BigDecimal("15000"));
        assertThat(entity.getIsAvailable()).isTrue();
        assertThat(entity.getIsMain()).isTrue();
        assertThat(entity.getIsPopular()).isTrue();
        assertThat(entity.getCalorie()).isEqualTo(650);
        assertThat(entity.getPurchaseCount()).isEqualTo(100);
        assertThat(entity.getWishlistCount()).isEqualTo(50);
        assertThat(entity.getReviewCount()).isEqualTo(20);
        assertThat(entity.getReviewRating()).isEqualByComparingTo(new BigDecimal("4.5"));
        assertThat(entity.getCreatedBy()).isEqualTo("owner");

        // 연관관계 컬렉션 초기화 확인
        assertThat(entity.getOptionGroups()).isEmpty();
        assertThat(entity.getCategoryRelations()).isEmpty();
    }

    @Test
    @DisplayName("MenuEntity를 도메인 Menu로 변환")
    void toDomain_ShouldConvertEntityToMenu() {
        // given
        // Restaurant 엔티티 생성
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .ownerId("1")
                .restaurantName("테스트 레스토랑")
                .isActive(true)
                .build();

        MenuEntity entity = MenuEntity.builder()
                .menuName("김치찌개")
                .description("얼큰한 김치찌개")
                .ingredients("김치, 돼지고기")
                .price(new BigDecimal("8000"))
                .isAvailable(true)
                .isMain(false)
                .isPopular(false)
                .calorie(450)
                .purchaseCount(200)
                .wishlistCount(30)
                .reviewCount(40)
                .reviewRating(new BigDecimal("4.2"))
                .createdBy("admin")
                .build();

        // 양방향 연관관계 설정
        restaurant.addMenu(entity);

        // when
        Menu domain = entity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getRestaurantId()).isEqualTo(restaurant.getId());
        assertThat(domain.getMenuName()).isEqualTo("김치찌개");
        assertThat(domain.getDescription()).isEqualTo("얼큰한 김치찌개");
        assertThat(domain.getIngredients()).isEqualTo("김치, 돼지고기");
        assertThat(domain.getPrice()).isEqualByComparingTo(new BigDecimal("8000"));
        assertThat(domain.getIsAvailable()).isTrue();
        assertThat(domain.getIsMain()).isFalse();
        assertThat(domain.getIsPopular()).isFalse();
        assertThat(domain.getCalorie()).isEqualTo(450);
        assertThat(domain.getPurchaseCount()).isEqualTo(200);
        assertThat(domain.getWishlistCount()).isEqualTo(30);
        assertThat(domain.getReviewCount()).isEqualTo(40);
        assertThat(domain.getReviewRating()).isEqualByComparingTo(new BigDecimal("4.2"));
        assertThat(domain.getCreatedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Menu와 Restaurant 양방향 연관관계 설정")
    void addMenu_ShouldSetBidirectionalRelation() {
        // given
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .ownerId("1")
                .restaurantName("테스트 레스토랑")
                .isActive(true)
                .build();

        MenuEntity menu = MenuEntity.builder()
                .menuName("떡볶이")
                .price(new BigDecimal("5000"))
                .isAvailable(true)
                .createdBy("owner")
                .build();

        // when
        restaurant.addMenu(menu);

        // then
        assertThat(restaurant.getMenus()).hasSize(1);
        assertThat(restaurant.getMenus()).contains(menu);
        assertThat(menu.getRestaurant()).isEqualTo(restaurant);
    }

    @Test
    @DisplayName("Menu에 OptionGroup 추가 시 양방향 연관관계 설정")
    void addOptionGroup_ShouldSetBidirectionalRelation() {
        // given
        MenuEntity menu = MenuEntity.builder()
                .menuName("아메리카노")
                .price(new BigDecimal("4000"))
                .isAvailable(true)
                .build();

        MenuOptionGroupEntity optionGroup = MenuOptionGroupEntity.builder()
                .restaurantId("REST123")
                .groupName("사이즈 선택")
                .minSelection(1)
                .maxSelection(1)
                .isRequired(true)
                .isActive(true)
                .build();

        // when
        menu.addOptionGroup(optionGroup);

        // then
        assertThat(menu.getOptionGroups()).hasSize(1);
        assertThat(menu.getOptionGroups()).contains(optionGroup);
        assertThat(optionGroup.getMenu()).isEqualTo(menu);
    }
}