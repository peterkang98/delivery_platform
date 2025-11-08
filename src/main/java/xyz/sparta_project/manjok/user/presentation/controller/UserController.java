package xyz.sparta_project.manjok.user.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.user.application.service.UserService;
import xyz.sparta_project.manjok.user.presentation.dto.UserAddressRequest;
import xyz.sparta_project.manjok.user.presentation.dto.UserAddressResponse;
import xyz.sparta_project.manjok.user.presentation.dto.UserResponse;

import java.util.List;

@Tag(name = "회원 관리 API", description = "회원 정보 조회 및 주소 관리 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("v1/users")
public class UserController {
	private final UserService userService;

	@Operation(summary = "내 정보 조회", description = "인증된 사용자의 회원 정보를 조회합니다.")
	@GetMapping
	public ResponseEntity<ApiResponse<UserResponse>> getUser() {
        System.out.println("인증");
        return ResponseEntity.ok(userService.getUser());
	}

	@Operation(summary = "특정 회원 정보 조회", description = "회원 ID를 기준으로 특정 사용자의 정보를 조회합니다.")
	@GetMapping("/{userId}")
	public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable String userId) {
		return ResponseEntity.ok(userService.getUser(userId));
	}

	@Operation(summary = "회원 삭제", description = "회원 ID를 기준으로 특정 회원 정보를 삭제합니다. (본인 또는 담당자/관리자만 수행 가능)")
	@DeleteMapping("/{userId}")
	public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable String userId) {
		userService.deleteUser(userId);
		return ResponseEntity.ok(userService.deleteUser(userId));
	}

	@Operation(summary = "회원 주소 추가", description = "회원 ID를 기준으로 새로운 주소를 추가합니다. (본인 또는 담당자/관리자만 수행 가능)")
	@PostMapping("/{userId}/addresses")
	public ResponseEntity<ApiResponse<List<UserAddressResponse>>> addAddress(
			@PathVariable String userId,
			@RequestBody UserAddressRequest request
	) {
		return ResponseEntity.ok(userService.addAddress(userId, request));
	}

	@Operation(summary = "회원 주소 수정", description = "회원의 주소 목록 중 지정한 인덱스의 주소를 수정합니다. (본인 또는 담당자/관리자만 수행 가능)")
	@PutMapping("/{userId}/addresses/{index}")
	public ResponseEntity<ApiResponse<List<UserAddressResponse>>> updateAddress(
			@PathVariable String userId,
			@PathVariable int index,
			@RequestBody UserAddressRequest request
	) {
		return ResponseEntity.ok(userService.updateAddress(userId, index, request));
	}

	@Operation(summary = "회원 주소 전체 교체", description = "회원의 주소 목록 전체를 요청 본문에 포함된 새로운 주소 목록으로 교체합니다.")
	@PutMapping("/{userId}/addresses")
	public ResponseEntity<ApiResponse<List<UserAddressResponse>>> replaceAllAddresses(
			@PathVariable String userId,
			@RequestBody List<UserAddressRequest> requests
	) {
		return ResponseEntity.ok(userService.replaceAllAddresses(userId, requests));
	}

	@Operation(summary = "회원 주소 삭제", description = "회원 주소 목록 중 지정한 인덱스의 주소를 삭제합니다.")
	@DeleteMapping("/{userId}/addresses/{index}")
	public ResponseEntity<ApiResponse<List<UserAddressResponse>>> deleteAddress(
			@PathVariable String userId,
			@PathVariable int index
	) {
		return ResponseEntity.ok(userService.deleteAddress(userId, index));
	}
}
