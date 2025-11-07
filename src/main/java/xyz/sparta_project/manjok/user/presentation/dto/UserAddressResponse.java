package xyz.sparta_project.manjok.user.presentation.dto;

import xyz.sparta_project.manjok.user.domain.vo.UserAddress;

public record UserAddressResponse(
	String address,
	double lat,
	double lon
){
	public static UserAddressResponse from(UserAddress address) {
		return new UserAddressResponse(address.getAddress(), address.getLat(), address.getLon());
	}
}
