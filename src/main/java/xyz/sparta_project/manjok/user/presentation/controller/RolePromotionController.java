package xyz.sparta_project.manjok.user.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "권한 승급 API", description = "사용자 권한 승급 요청 및 관리 기능 제공")
@RestController
@RequiredArgsConstructor
public class RolePromotionController {
	private final RolePromotionService promotionService;

	@Operation(summary = "권한 승급 요청", description = "사용자가 자신의 권한 승급을 요청합니다.")
	@PostMapping("/v1/role-promotion-requests")
	public ResponseEntity<ApiResponse<?>> requestRolePromotion(@Valid RolePromotionRequestDto dto) {
		return ResponseEntity.ok(promotionService.saveRequest(dto));
	}

	@Operation(summary = "내 권한 승급 요청 이력 조회", description = "로그인한 사용자가 과거에 요청한 권한 승급 이력을 조회합니다.")
	@GetMapping("/v1/role-promotion-requests")
	public ResponseEntity<ApiResponse<List<RolePromotionResponse>>> getRolePromotionHistory() {
		return ResponseEntity.ok(promotionService.getRequestHistory());
	}

	@Operation(summary = "관리자: 권한 승급 요청 조회", description = "관리자가 모든 사용자의 권한 승급 요청을 조회합니다. status 파라미터를 전달하면 특정 상태(PENDING, ACCEPTED, REJECTED)만 필터링 가능합니다.(페이징 및 정렬 지원) ")
	@GetMapping("/v1/admin/role-promotion-requests")
	public ResponseEntity<PageResponse<RolePromotionAdminResponse>> viewAllRequestsForAdmin(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "createdDate") String sortBy,
			@RequestParam(defaultValue = "desc") String direction,
			@RequestParam(required = false) PromotionRequestStatus status
	) {
		Sort sort = Sort.by(direction.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
		return ResponseEntity.ok(promotionService.getRequestsByStatus(status, PageRequest.of(page, 10, sort)));
	}

	@Operation(summary = "관리자: 권한 승급 요청 승인/거절", description = "관리자가 특정 사용자의 권한 승급 요청을 승인하거나 거절합니다.")
	@PostMapping("/v1/admin/role-promotion-requests")
	public ResponseEntity<ApiResponse<?>> requestRolePromotion(RolePromotionAdminRequestDto dto) {
		return ResponseEntity.ok(promotionService.reviewRequest(dto));
	}
}
