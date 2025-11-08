package xyz.sparta_project.manjok.user.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.global.infrastructure.security.SecurityUtils;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.global.presentation.exception.GlobalErrorCode;
import xyz.sparta_project.manjok.user.domain.entity.User;
import xyz.sparta_project.manjok.user.domain.repository.UserRepository;
import xyz.sparta_project.manjok.user.domain.vo.Role;
import xyz.sparta_project.manjok.user.domain.vo.UserAddress;
import xyz.sparta_project.manjok.user.exception.UserErrorCode;
import xyz.sparta_project.manjok.user.exception.UserException;
import xyz.sparta_project.manjok.user.infrastructure.security.userdetails.CustomUserDetails;
import xyz.sparta_project.manjok.user.presentation.dto.UserAddressRequest;
import xyz.sparta_project.manjok.user.presentation.dto.UserAddressResponse;
import xyz.sparta_project.manjok.user.presentation.dto.UserResponse;

import java.util.List;

import static xyz.sparta_project.manjok.user.exception.UserErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
	private final UserRepository userRepository;

	// User
	public ApiResponse<UserResponse> getUser() {
		User user = SecurityUtils.getCurrentUserDetails().map(CustomUserDetails::getUser)
								 .orElseThrow(() -> new UserException(GlobalErrorCode.INVALID_SECURITY_CONTEXT));
		return ApiResponse.success(new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getAddresses()));
	}

	public ApiResponse<UserResponse> getUser(String userId) {
		User user = findUser(userId);
		return ApiResponse.success(new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getAddresses()));
	}

	public ApiResponse<?> deleteUser(String userId) {
		User user = findUser(userId);
		checkAuthorization(user);
		user.softDelete(SecurityUtils.getCurrentUserId().orElse("SYSTEM"));
		return ApiResponse.success(null, "사용자가 삭제되었습니다.");
	}

	// Address
	public ApiResponse<List<UserAddressResponse>> getAddresses(String userId) {
		User user = findUser(userId);
		checkAuthorization(user);

		return ApiResponse.success(user.getAddresses().stream()
				   .map(UserAddressResponse::from)
				   .toList());
	}

	public ApiResponse<List<UserAddressResponse>> addAddress(String userId, UserAddressRequest request) {
		User user = findUser(userId);
		checkAuthorization(user);

		user.addAddress(request.toEntity());
		return ApiResponse.success(user.getAddresses().stream()
									   .map(UserAddressResponse::from)
									   .toList());
	}

	public ApiResponse<List<UserAddressResponse>> updateAddress(String userId, int index, UserAddressRequest request) {
		User user = findUser(userId);
		checkAuthorization(user);

		user.updateAddress(index, request.toEntity());
		return ApiResponse.success(user.getAddresses().stream()
				   .map(UserAddressResponse::from)
				   .toList());
	}

	public ApiResponse<List<UserAddressResponse>> replaceAllAddresses(String userId, List<UserAddressRequest> requests) {
		User user = findUser(userId);
		checkAuthorization(user);

		user.emptyAddress();

		for (UserAddressRequest r : requests) {
			user.addAddress(r.toEntity());
		}
		return ApiResponse.success(user.getAddresses().stream()
				   .map(UserAddressResponse::from)
				   .toList());
	}

	public ApiResponse<List<UserAddressResponse>> deleteAddress(String userId, int index) {
		User user = findUser(userId);
		checkAuthorization(user);

		List<UserAddress> addresses = user.getAddresses();
		if (addresses == null || index < 0 || index >= addresses.size()) {
			throw new UserException(GlobalErrorCode.INVALID_INPUT_VALUE);
		}
		user.removeAddress(addresses.get(index));

		return ApiResponse.success(user.getAddresses().stream()
				   .map(UserAddressResponse::from)
				   .toList());
	}

	private static void checkAuthorization(User foundUser) {
		String currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new UserException(GlobalErrorCode.INVALID_SECURITY_CONTEXT));
		Role currentRole = SecurityUtils.getCurrentRole()
								 .orElseThrow(() -> new UserException(GlobalErrorCode.INVALID_SECURITY_CONTEXT));

		// 본인이거나, 서비스 담당자, 최종관리자이면 사용자 정보 CRUD 가능
		if (!currentUserId.equals(foundUser.getId()) && currentRole.hasHigherAuthorityThan(Role.OWNER)){
			throw new UserException(UNAUTHORIZED_USER);
		}
	}

	private User findUser(String userId) {
		return userRepository.findById(userId).orElseThrow(() -> new UserException(INVALID_USER_ID));
	}
}
