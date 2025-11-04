package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.MenuOption;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MenuOptionEntity 변환 테스트 (연관관계 매핑 적용)
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
        // OptionGroup 연관관계는 null (OptionGroup에서 설정해야 함)
        assertThat(entity.getOptionGroup()).isNull();
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
        // OptionGroup 엔티티 생성
        MenuOptionGroupEntity optionGroup = MenuOptionGroupEntity.builder()
                .restaurantId("REST345")
                .groupName("맵기 선택")
                .minSelection(1)
                .maxSelection(1)
                .isRequired(true)
                .isActive(true)
                .build();

        MenuOptionEntity entity = MenuOptionEntity.builder()
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

        // 양방향 연관관계 설정
        optionGroup.addOption(entity);

        // when
        MenuOption domain = entity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getOptionGroupId()).isEqualTo(optionGroup.getId());
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

    @Test
    @DisplayName("MenuOption과 MenuOptionGroup 양방향 연관관계 설정")
    void addOption_ShouldSetBidirectionalRelation() {
        // given
        MenuOptionGroupEntity optionGroup = MenuOptionGroupEntity.builder()
                .restaurantId("REST123")
                .groupName("사이즈 선택")
                .minSelection(1)
                .maxSelection(1)
                .isRequired(true)
                .isActive(true)
                .build();

        MenuOptionEntity option = MenuOptionEntity.builder()
                .menuId("MENU456")
                .restaurantId("REST123")
                .optionName("Medium")
                .additionalPrice(new BigDecimal("500"))
                .isAvailable(true)
                .isDefault(true)
                .displayOrder(2)
                .build();

        // when
        optionGroup.addOption(option);

        // then
        assertThat(optionGroup.getOptions()).hasSize(1);
        assertThat(optionGroup.getOptions()).contains(option);
        assertThat(option.getOptionGroup()).isEqualTo(optionGroup);
    }
}