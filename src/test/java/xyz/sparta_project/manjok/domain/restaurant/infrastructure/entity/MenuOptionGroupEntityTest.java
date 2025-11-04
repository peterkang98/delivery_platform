package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.MenuOptionGroup;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MenuOptionGroupEntity 변환 테스트
 */
class MenuOptionGroupEntityTest {

    @Test
    @DisplayName("도메인 MenuOptionGroup을 MenuOptionGroupEntity로 변환")
    void fromDomain_ShouldConvertMenuOptionGroupToEntity() {
        // given
        MenuOptionGroup domain = MenuOptionGroup.builder()
                .id("OPTIONGROUP123")
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .menuId("MENU456")
                .restaurantId("REST789")
                .groupName("사이즈 선택")
                .description("음료 사이즈")
                .minSelection(1)
                .maxSelection(1)
                .isRequired(true)
                .displayOrder(1)
                .isActive(true)
                .createdBy("owner")
                .build();

        // when
        MenuOptionGroupEntity entity = MenuOptionGroupEntity.fromDomain(domain);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo("OPTIONGROUP123");
        assertThat(entity.getMenuId()).isEqualTo("MENU456");
        assertThat(entity.getRestaurantId()).isEqualTo("REST789");
        assertThat(entity.getGroupName()).isEqualTo("사이즈 선택");
        assertThat(entity.getDescription()).isEqualTo("음료 사이즈");
        assertThat(entity.getMinSelection()).isEqualTo(1);
        assertThat(entity.getMaxSelection()).isEqualTo(1);
        assertThat(entity.getIsRequired()).isTrue();
        assertThat(entity.getDisplayOrder()).isEqualTo(1);
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getCreatedBy()).isEqualTo("owner");
    }

    @Test
    @DisplayName("MenuOptionGroupEntity를 도메인 MenuOptionGroup으로 변환")
    void toDomain_ShouldConvertEntityToMenuOptionGroup() {
        // given
        MenuOptionGroupEntity entity = MenuOptionGroupEntity.builder()
                .menuId("MENU012")
                .restaurantId("REST345")
                .groupName("토핑 추가")
                .description("추가 토핑")
                .minSelection(0)
                .maxSelection(3)
                .isRequired(false)
                .displayOrder(2)
                .isActive(true)
                .createdBy("admin")
                .build();

        // when
        MenuOptionGroup domain = entity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getMenuId()).isEqualTo("MENU012");
        assertThat(domain.getRestaurantId()).isEqualTo("REST345");
        assertThat(domain.getGroupName()).isEqualTo("토핑 추가");
        assertThat(domain.getDescription()).isEqualTo("추가 토핑");
        assertThat(domain.getMinSelection()).isEqualTo(0);
        assertThat(domain.getMaxSelection()).isEqualTo(3);
        assertThat(domain.getIsRequired()).isFalse();
        assertThat(domain.getDisplayOrder()).isEqualTo(2);
        assertThat(domain.getIsActive()).isTrue();
        assertThat(domain.getCreatedBy()).isEqualTo("admin");
    }
}