package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.MenuOption;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MenuOptionEntity 변환 테스트
 */
class MenuOptionEntityTest {

    @Test
    @DisplayName("도메인 MenuOption을 MenuOptionEntity로 변환")
    void fromDomain_ShouldConvertMenuOptionToEntity() {
        // given
        MenuOption domain = MenuOption.builder()
                .id("OPTION123")
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .optionGroupId("GROUP456")
                .menuId("MENU789")
                .restaurantId("REST012")
                .optionName("Large")
                .description("큰 사이즈")
                .additionalPrice(new BigDecimal("1000"))
                .isAvailable(true)
                .isDefault(false)
                .displayOrder(1)
                .purchaseCount(50)
                .createdBy("owner")
                .build();

        // when
        MenuOptionEntity entity = MenuOptionEntity.fromDomain(domain);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo("OPTION123");
        assertThat(entity.getOptionGroupId()).isEqualTo("GROUP456");
        assertThat(entity.getMenuId()).isEqualTo("MENU789");
        assertThat(entity.getRestaurantId()).isEqualTo("REST012");
        assertThat(entity.getOptionName()).isEqualTo("Large");
        assertThat(entity.getDescription()).isEqualTo("큰 사이즈");
        assertThat(entity.getAdditionalPrice()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(entity.getIsAvailable()).isTrue();
        assertThat(entity.getIsDefault()).isFalse();
        assertThat(entity.getDisplayOrder()).isEqualTo(1);
        assertThat(entity.getPurchaseCount()).isEqualTo(50);
        assertThat(entity.getCreatedBy()).isEqualTo("owner");
    }

    @Test
    @DisplayName("MenuOptionEntity를 도메인 MenuOption으로 변환")
    void toDomain_ShouldConvertEntityToMenuOption() {
        // given
        MenuOptionEntity entity = MenuOptionEntity.builder()
                .optionGroupId("GROUP789")
                .menuId("MENU012")
                .restaurantId("REST345")
                .optionName("매운맛")
                .description("매운 양념")
                .additionalPrice(new BigDecimal("0"))
                .isAvailable(true)
                .isDefault(true)
                .displayOrder(1)
                .purchaseCount(100)
                .createdBy("admin")
                .build();

        // when
        MenuOption domain = entity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getOptionGroupId()).isEqualTo("GROUP789");
        assertThat(domain.getMenuId()).isEqualTo("MENU012");
        assertThat(domain.getRestaurantId()).isEqualTo("REST345");
        assertThat(domain.getOptionName()).isEqualTo("매운맛");
        assertThat(domain.getDescription()).isEqualTo("매운 양념");
        assertThat(domain.getAdditionalPrice()).isEqualByComparingTo(new BigDecimal("0"));
        assertThat(domain.getIsAvailable()).isTrue();
        assertThat(domain.getIsDefault()).isTrue();
        assertThat(domain.getDisplayOrder()).isEqualTo(1);
        assertThat(domain.getPurchaseCount()).isEqualTo(100);
        assertThat(domain.getCreatedBy()).isEqualTo("admin");
    }
}