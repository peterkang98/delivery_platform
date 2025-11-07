package xyz.sparta_project.manjok.user.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.global.common.utils.PageUtils;
import xyz.sparta_project.manjok.global.infrastructure.security.SecurityUtils;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.global.presentation.dto.PageResponse;
import xyz.sparta_project.manjok.global.presentation.exception.GlobalErrorCode;
import xyz.sparta_project.manjok.user.domain.entity.RolePromotionRequest;
import xyz.sparta_project.manjok.user.domain.entity.User;
import xyz.sparta_project.manjok.user.domain.repository.RolePromotionRequestRepository;
import xyz.sparta_project.manjok.user.domain.repository.UserRepository;
import xyz.sparta_project.manjok.user.domain.service.RoleCheck;
import xyz.sparta_project.manjok.user.domain.vo.PromotionRequestStatus;
import xyz.sparta_project.manjok.user.domain.vo.Role;
import xyz.sparta_project.manjok.user.exception.UserErrorCode;
import xyz.sparta_project.manjok.user.exception.UserException;
import xyz.sparta_project.manjok.user.presentation.dto.RolePromotionAdminRequestDto;
import xyz.sparta_project.manjok.user.presentation.dto.RolePromotionAdminResponse;
import xyz.sparta_project.manjok.user.presentation.dto.RolePromotionRequestDto;
import xyz.sparta_project.manjok.user.presentation.dto.RolePromotionResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RolePromotionService {
	private final RoleCheck roleCheck;
	private final UserRepository repository;
	private final RolePromotionRequestRepository rolePromotionRequestRepository;


	// 담당자, 관리자
	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
	public PageResponse<RolePromotionAdminResponse> getRequestsByStatus(PromotionRequestStatus status, Pageable pageable) {
		Page<RolePromotionRequest> requests;

		if (status == null) {
			requests = rolePromotionRequestRepository.findAll(pageable);
		} else {
			requests = rolePromotionRequestRepository.findAllByStatus(status, pageable);
		}
		return PageUtils.toPageResponse(requests, RolePromotionAdminResponse::from);
	}

	@PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
	public ApiResponse<?> reviewRequest(RolePromotionAdminRequestDto dto) {
		RolePromotionRequest request = rolePromotionRequestRepository.findById(dto.requestId())
														.orElseThrow(() -> new UserException(GlobalErrorCode.INTERNAL_SERVER_ERROR));
		request.review(getCurrentUser(), dto.reviewComment(), dto.action());
		User requestUser = repository.findById(dto.requestUserId()).orElseThrow(() -> new UserException(UserErrorCode.INVALID_USER_ID));
		requestUser.promoteRole(request.getRequestedRole(), roleCheck);

		// 사용자에게 이메일 알림

		return ApiResponse.success(null, "권한 승급 검토가 완료되었습니다.");
	}

	// 일반 사용자
	public ApiResponse<?> saveRequest(RolePromotionRequestDto dto) {
		Role currentRole = SecurityUtils.getCurrentRole().orElseThrow(() -> new UserException(GlobalErrorCode.INVALID_SECURITY_CONTEXT));
		User currentUser = getCurrentUser();

		RolePromotionRequest request = RolePromotionRequest.builder()
														   .currentUser(currentUser)
														   .currentRole(currentRole)
														   .requestedRole(dto.requestedRole())
														   .reason(dto.reason())
														   .businessRegistrationNum(dto.businessRegistrationNum())
														   .build();

		rolePromotionRequestRepository.save(request);
		return ApiResponse.success(null, "권한 승급 요청이 완료되었습니다. 담당자가 확인 후 이메일을 보내드리겠습니다.");
	}

	@Transactional(readOnly = true)
	public ApiResponse<List<RolePromotionResponse>> getRequestHistory() {
		List<RolePromotionRequest> promotionRequests = rolePromotionRequestRepository.findByRequester(getCurrentUser());
		List<RolePromotionResponse> responseList = promotionRequests.stream().map(RolePromotionResponse::from).toList();
		return ApiResponse.success(responseList);
	}

	private User getCurrentUser() {
		return SecurityUtils.getCurrentUserDetails()
							.orElseThrow(() -> new UserException(GlobalErrorCode.INVALID_SECURITY_CONTEXT))
							.getUser();
	}
}
