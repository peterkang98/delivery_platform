package xyz.sparta_project.manjok.user.presentation.dto;

import xyz.sparta_project.manjok.user.domain.vo.UserAddress;

import java.util.List;

public record UserResponse(
    String userId,
	String username,
	String email,
	List<UserAddress> addresses
) {
}
