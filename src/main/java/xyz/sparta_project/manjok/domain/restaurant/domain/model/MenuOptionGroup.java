package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.MenuErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MenuOptionGroup Entity
 * - 메뉴의 옵션들을 그룹화하여 관리
 * - 예: "사이즈 선택", "맵기 선택", "추가 토핑" 등
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"id"})
public class MenuOptionGroup {

    // 식별자
    private String id;
    private LocalDateTime createdAt;

    // 소속 정보
    private String menuId;              // 소속 메뉴 ID
    private String restaurantId;        // 레스토랑 ID

    // 그룹 정보
    private String groupName;           // 그룹명 (예: "사이즈 선택")
    private String description;         // 그룹 설명

    // 선택 규칙
    @Builder.Default
    private Integer minSelection = 0;   // 최소 선택 개수 (0이면 선택사항)

    @Builder.Default
    private Integer maxSelection = 1;   // 최대 선택 개수 (1이면 단일선택, 여러개면 다중선택)

    @Builder.Default
    private Boolean isRequired = false; // 필수 선택 여부

    // 표시 정보
    @Builder.Default
    private Integer displayOrder = 0;   // 표시 순서

    @Builder.Default
    private Boolean isActive = true;    // 활성 상태

    // 옵션 관리 (하위 엔티티 직접 관리)
    @Builder.Default
    private List<MenuOption> options = new ArrayList<>();

    // 감사 필드
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    @Builder.Default
    private Boolean isDeleted = false;
    private LocalDateTime deletedAt;
    private String deletedBy;

    /**
     * 옵션 추가
     */
    public MenuOption addOption(String optionName, Integer additionalPrice,
                                Integer displayOrder, String createdBy) {
        MenuOption option = MenuOption.builder()
                .id(null)  // ID는 나중에 생성
                .createdAt(LocalDateTime.now())
                .optionGroupId(this.id)
                .menuId(this.menuId)
                .restaurantId(this.restaurantId)
                .optionName(optionName)
                .additionalPrice(new BigDecimal(additionalPrice))
                .displayOrder(displayOrder)
                .isAvailable(true)
                .isDefault(false)
                .createdBy(createdBy)
                .build();

        this.options.add(option);
        return option;
    }

    /**
     * 옵션 제거
     */
    public void removeOption(String optionId) {
        this.options.removeIf(option -> option.getId().equals(optionId));
    }

    /**
     * 선택 규칙 검증
     */
    public void validateSelectionRule() {
        if (minSelection < 0) {
            throw new RestaurantException(MenuErrorCode.INVALID_MAX_SELECTION,
                    "최소 선택 개수는 0 이상이어야 합니다.");
        }

        if (maxSelection < minSelection) {
            throw new RestaurantException(MenuErrorCode.INVALID_MAX_SELECTION);
        }

        if (isRequired && minSelection == 0) {
            this.minSelection = 1; // 필수인 경우 최소 1개는 선택해야 함
        }
    }

    /**
     * 그룹 정보 업데이트
     */
    public void update(String groupName, String description,
                       Boolean isRequired, Integer minSelection, Integer maxSelection,
                       String updatedBy) {
        this.groupName = groupName;
        this.description = description;
        this.isRequired = isRequired;
        this.minSelection = minSelection;
        this.maxSelection = maxSelection;
        validateSelectionRule();
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 활성/비활성 설정
     */
    public void setActive(boolean active, String updatedBy) {
        this.isActive = active;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * Soft Delete
     */
    public void delete(String deletedBy) {
        this.isDeleted = true;
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        // 하위 옵션들도 삭제 처리
        this.options.forEach(option -> option.delete(deletedBy));
    }

    /**
     * 사용 가능한지 확인
     */
    public boolean isAvailable() {
        return isActive && !isDeleted;
    }

    /**
     * 활성화된 옵션 개수
     */
    public int getActiveOptionCount() {
        return (int) options.stream()
                .filter(MenuOption::isAvailable)
                .count();
    }

}
