package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Set;

/**
 * Admin용 Menu 수정 요청 DTO
 * - 관리자가 모든 필드를 수정 가능
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMenuUpdateRequest {

    private String menuName;

    private String description;

    private String ingredients;

    @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    private BigDecimal price;

    // 카테고리 ID 목록
    private Set<String> categoryIds;

    // 주 카테고리 ID
    private String primaryCategoryId;

    // 영양 정보
    private Integer calorie;

    // 상태 정보
    private Boolean isAvailable;
    private Boolean isMain;
    private Boolean isPopular;
    private Boolean isNew;
}