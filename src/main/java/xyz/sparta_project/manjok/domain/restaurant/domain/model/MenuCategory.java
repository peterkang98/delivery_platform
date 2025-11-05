package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * MenuCategory Entity
 * - 메뉴 분류 (메인, 사이드, 음료 등)
 * - 셀프 조인으로 계층 구조 구현
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"id"})
public class MenuCategory {

    // 식별자
    private String id;
    private LocalDateTime createdAt;

    // 기본 정보
    private String restaurantId;        // 레스토랑별 카테고리 관리
    private String categoryName;        // 카테고리명 (메인, 사이드, 음료 등)
    private String description;         // 카테고리 설명

    // 계층 구조 (ID 참조 방식)
    private String parentCategoryId;    // 상위 카테고리 ID (null이면 최상위)
    private Integer depth;              // 계층 깊이 (1: 대분류, 2: 중분류, 3: 소분류)
    private Integer displayOrder;       // 표시 순서

    // 상태 정보
    @Builder.Default
    private Boolean isActive = true;   // 활성 상태

    // 연관 관계 (ID 참조)
    @Builder.Default
    private Set<String> menuIds = new HashSet<>();  // 해당 카테고리의 메뉴 ID 목록

    // 감사 필드
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    @Builder.Default
    private Boolean isDeleted = false;
    private LocalDateTime deletedAt;
    private String deletedBy;

    /**
     * 메뉴 추가
     */
    public void addMenu(String menuId) {
        if (menuId != null) {
            this.menuIds.add(menuId);
        }
    }

    /**
     * 메뉴 제거
     */
    public void removeMenu(String menuId) {
        this.menuIds.remove(menuId);
    }

    /**
     * 카테고리 정보 업데이트
     */
    public void update(String categoryName, String description,
                       Integer displayOrder, String updatedBy) {
        this.categoryName = categoryName;
        this.description = description;
        this.displayOrder = displayOrder;
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
    }

    /**
     * 최상위 카테고리인지 확인
     */
    public boolean isRootCategory() {
        return parentCategoryId == null || depth == 1;
    }

    /**
     * 사용 가능한지 확인
     */
    public boolean isAvailable() {
        return isActive && !isDeleted;
    }
}
