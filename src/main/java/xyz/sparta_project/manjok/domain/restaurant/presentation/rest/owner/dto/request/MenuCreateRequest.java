package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.owner.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Set;

/**
 * Owner용 Menu 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuCreateRequest {

    @NotBlank(message = "메뉴명은 필수입니다.")
    private String menuName;

    private String description;

    private String ingredients;

    @NotNull(message = "가격은 필수입니다.")
    @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    private BigDecimal price;

    // 카테고리 ID 목록
    private Set<String> categoryIds;

    // 주 카테고리 ID (하나만 지정)
    private String primaryCategoryId;

    // 영양 정보
    private Integer calorie;

    // 상태 정보
    private Boolean isMain;
    private Boolean isPopular;
    private Boolean isNew;
}