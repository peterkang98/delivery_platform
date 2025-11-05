package xyz.sparta_project.manjok.domain.restaurant.presentation.rest.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Restaurant 상태 변경 요청 DTO
 * - 관리자가 식당 상태를 변경할 때 사용
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantStatusUpdateRequest {

    @NotBlank(message = "상태 값은 필수입니다.")
    private String status; // OPEN, CLOSED, TEMPORARILY_CLOSED, PREPARING
}