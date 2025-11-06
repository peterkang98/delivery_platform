package xyz.sparta_project.manjok.user.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;
import xyz.sparta_project.manjok.user.application.service.RolePromotionService;
import xyz.sparta_project.manjok.user.domain.vo.PromotionRequestStatus;
import xyz.sparta_project.manjok.user.presentation.dto.RolePromotionAdminRequestDto;
import xyz.sparta_project.manjok.user.presentation.dto.RolePromotionAdminResponse;
import xyz.sparta_project.manjok.user.presentation.dto.RolePromotionRequestDto;
import xyz.sparta_project.manjok.user.presentation.dto.RolePromotionResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RolePromotionController {
	private final RolePromotionService promotionService;

	// 사용자가 권한 승급을 요청
	@PostMapping("/v1/role-promotion-requests")
	public ResponseEntity<ApiResponse<?>> requestRolePromotion(@Valid RolePromotionRequestDto dto) {
		return ResponseEntity.ok(promotionService.saveRequest(dto));
	}

	// 특정 사용자의 권한 승급 요청 이력 조회
	@GetMapping("/v1/role-promotion-requests")
	public ResponseEntity<ApiResponse<List<RolePromotionResponse>>> getRolePromotionHistory() {
		return ResponseEntity.ok(promotionService.getRequestHistory());
	}

	// 관리자가 모든 권한 승급 요청을 조회
	// 아직 검토 되지 않은 요청만 조회
	// 페이징, 정렬 기능 추가
	@GetMapping("/v1/admin/role-promotion-requests")
	public ResponseEntity<PageResponse<RolePromotionAdminResponse>> viewAllRequestsForAdmin(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "createdDate") String sortBy,
			@RequestParam(defaultValue = "desc") String direction
	) {
		Sort sort = Sort.by(direction.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
		return ResponseEntity.ok(promotionService.getRequestsByStatus(null, PageRequest.of(page, 10, sort)));
	}

	@PostMapping("/v1/admin/role-promotion-requests")
	public ResponseEntity<ApiResponse<?>> requestRolePromotion(RolePromotionAdminRequestDto dto) {
		return ResponseEntity.ok(promotionService.reviewRequest(dto));
	}
}
