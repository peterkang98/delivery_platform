package xyz.sparta_project.manjok.user.presentation.dto;

import xyz.sparta_project.manjok.user.domain.vo.UserAddress;

public record UserAddressRequest (
		String address,
		double lat,
		double lon
){
	public UserAddress toEntity() {
		return new UserAddress(address(), lat(), lon());
	}
}
