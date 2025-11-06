package xyz.sparta_project.manjok.user.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAddress {
	@Column(length=100, nullable = false)
	private String address;
	private double lat;
	private double lon;

	@Builder
	public UserAddress(String address, double lat, double lon) {
		this.address = address;
		this.lat = lat;
		this.lon = lon;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof UserAddress)) return false;
		UserAddress that = (UserAddress) o;
		return Double.compare(that.lat, lat) == 0 &&
				Double.compare(that.lon, lon) == 0 &&
				Objects.equals(address, that.address);
	}

	@Override
	public int hashCode() {
		return Objects.hash(address, lat, lon);
	}

}
