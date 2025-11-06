package xyz.sparta_project.manjok.user.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.sparta_project.manjok.global.presentation.dto.ApiResponse;
import xyz.sparta_project.manjok.user.application.service.UserService;
import xyz.sparta_project.manjok.user.presentation.dto.UserAddressRequest;
import xyz.sparta_project.manjok.user.presentation.dto.UserAddressResponse;
import xyz.sparta_project.manjok.user.presentation.dto.UserResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/users/{userId}")
public class UserController {
	private final UserService userService;

	@GetMapping
	public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable String userId) {
		return ResponseEntity.ok(userService.getUser(userId));
	}

	@DeleteMapping()
	public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable String userId) {
		userService.deleteUser(userId);
		return ResponseEntity.ok(userService.deleteUser(userId));
	}

	@PostMapping("/addresses")
	public ResponseEntity<ApiResponse<List<UserAddressResponse>>> addAddress(
			@PathVariable String userId,
			@RequestBody UserAddressRequest request
	) {
		return ResponseEntity.ok(userService.addAddress(userId, request));
	}

	@PutMapping("/addresses/{index}")
	public ResponseEntity<ApiResponse<List<UserAddressResponse>>> updateAddress(
			@PathVariable String userId,
			@PathVariable int index,
			@RequestBody UserAddressRequest request
	) {
		return ResponseEntity.ok(userService.updateAddress(userId, index, request));
	}

	@PutMapping("/addresses")
	public ResponseEntity<ApiResponse<List<UserAddressResponse>>> replaceAllAddresses(
			@PathVariable String userId,
			@RequestBody List<UserAddressRequest> requests
	) {
		return ResponseEntity.ok(userService.replaceAllAddresses(userId, requests));
	}

	@DeleteMapping("/addresses/{index}")
	public ResponseEntity<ApiResponse<List<UserAddressResponse>>> deleteAddress(
			@PathVariable String userId,
			@PathVariable int index
	) {
		return ResponseEntity.ok(userService.deleteAddress(userId, index));
	}
}
