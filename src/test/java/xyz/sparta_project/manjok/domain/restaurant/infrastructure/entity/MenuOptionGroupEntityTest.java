package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.MenuOptionGroup;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MenuOptionGroupEntity 변환 테스트 (연관관계 매핑 적용)
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
        // Menu 연관관계는 null (Menu에서 설정해야 함)
        assertThat(entity.getMenu()).isNull();
        assertThat(entity.getRestaurantId()).isEqualTo("REST789");
        assertThat(entity.getGroupName()).isEqualTo("사이즈 선택");
        assertThat(entity.getDescription()).isEqualTo("음료 사이즈");
        assertThat(entity.getMinSelection()).isEqualTo(1);
        assertThat(entity.getMaxSelection()).isEqualTo(1);
        assertThat(entity.getIsRequired()).isTrue();
        assertThat(entity.getDisplayOrder()).isEqualTo(1);
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getCreatedBy()).isEqualTo("owner");

        // 연관관계 컬렉션 초기화 확인
        assertThat(entity.getOptions()).isEmpty();
    }

    @Test
    @DisplayName("MenuOptionGroupEntity를 도메인 MenuOptionGroup으로 변환")
    void toDomain_ShouldConvertEntityToMenuOptionGroup() {
        // given
        // Menu 엔티티 생성
        MenuEntity menu = MenuEntity.builder()
                .menuName("아메리카노")
                .price(new BigDecimal("4000"))
                .isAvailable(true)
                .build();

        MenuOptionGroupEntity entity = MenuOptionGroupEntity.builder()
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

        // 양방향 연관관계 설정
        menu.addOptionGroup(entity);

        // when
        MenuOptionGroup domain = entity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getMenuId()).isEqualTo(menu.getId());
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

    @Test
    @DisplayName("MenuOptionGroup과 Menu 양방향 연관관계 설정")
    void addOptionGroup_ShouldSetBidirectionalRelation() {
        // given
        MenuEntity menu = MenuEntity.builder()
                .menuName("카페라떼")
                .price(new BigDecimal("4500"))
                .isAvailable(true)
                .build();

        MenuOptionGroupEntity optionGroup = MenuOptionGroupEntity.builder()
                .restaurantId("REST123")
                .groupName("온도 선택")
                .description("HOT/ICED")
                .minSelection(1)
                .maxSelection(1)
                .isRequired(true)
                .displayOrder(1)
                .isActive(true)
                .build();

        // when
        menu.addOptionGroup(optionGroup);

        // then
        assertThat(menu.getOptionGroups()).hasSize(1);
        assertThat(menu.getOptionGroups()).contains(optionGroup);
        assertThat(optionGroup.getMenu()).isEqualTo(menu);
    }

    @Test
    @DisplayName("MenuOptionGroup에 Option 추가 시 양방향 연관관계 설정")
    void addOption_ShouldSetBidirectionalRelation() {
        // given
        MenuOptionGroupEntity optionGroup = MenuOptionGroupEntity.builder()
                .restaurantId("REST456")
                .groupName("사이즈 선택")
                .minSelection(1)
                .maxSelection(1)
                .isRequired(true)
                .isActive(true)
                .build();

        MenuOptionEntity option1 = MenuOptionEntity.builder()
                .menuId("MENU789")
                .restaurantId("REST456")
                .optionName("Small")
                .additionalPrice(new BigDecimal("0"))
                .isAvailable(true)
                .displayOrder(1)
                .build();

        MenuOptionEntity option2 = MenuOptionEntity.builder()
                .menuId("MENU789")
                .restaurantId("REST456")
                .optionName("Large")
                .additionalPrice(new BigDecimal("1000"))
                .isAvailable(true)
                .displayOrder(2)
                .build();

        // when
        optionGroup.addOption(option1);
        optionGroup.addOption(option2);

        // then
        assertThat(optionGroup.getOptions()).hasSize(2);
        assertThat(optionGroup.getOptions()).containsExactly(option1, option2);
        assertThat(option1.getOptionGroup()).isEqualTo(optionGroup);
        assertThat(option2.getOptionGroup()).isEqualTo(optionGroup);
    }
}