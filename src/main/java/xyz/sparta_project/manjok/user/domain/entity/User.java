package xyz.sparta_project.manjok.user.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import xyz.sparta_project.manjok.global.common.dto.BaseEntity;
import xyz.sparta_project.manjok.global.presentation.exception.GlobalErrorCode;
import xyz.sparta_project.manjok.user.domain.service.RoleCheck;
import xyz.sparta_project.manjok.user.domain.vo.Role;
import xyz.sparta_project.manjok.user.domain.vo.UserAddress;
import xyz.sparta_project.manjok.user.exception.UserErrorCode;
import xyz.sparta_project.manjok.user.exception.UserException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "p_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseEntity {
	@Column(length = 100, nullable = false, unique = true)
	private String email;

	@Column(length = 10, nullable = false, unique = true)
	private String username;

	@Column(length = 60, nullable = false)
	private String password;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Role role = Role.CUSTOMER;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="p_user_address", joinColumns = @JoinColumn(name="user_id"))
	@OrderColumn(name="address_idx")
	private List<UserAddress> addresses;

	@OneToMany(mappedBy = "requester")
	private List<RolePromotionRequest> sentRequests = new ArrayList<>();

	@OneToMany(mappedBy = "reviewer")
	private List<RolePromotionRequest> reviewedRequests = new ArrayList<>();

	@Column(name = "is_verified", nullable = false)
	private Boolean isVerified = false;

	@Column(name = "is_public", nullable = false)
	private Boolean isPublic = false;

	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "updated_by", length = 36)
	private String updatedBy;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "deleted_by", length = 36)
	private String deletedBy;

	@Column(name = "created_by", length = 36)
	private String createdBy;

	@Builder
	public User(String username, String email, String password) {
		this.username = username;
		this.email = email;
		this.password = password;
	}

	public void verify() {
		this.isVerified = true;
		this.isPublic = true;
	}

	public void updatePassword(String newPassword) {
		this.password = newPassword;
	}

	public void promoteRole(Role newRole, RoleCheck roleCheck) {
		if (!roleCheck.check()) {
			throw new UserException(GlobalErrorCode.FORBIDDEN); //접근 권한 없음
		}
		this.role = newRole;
	}

	public void addAddress(UserAddress address) {
		addresses = toModifiableList(addresses);
		addresses.add(address);
	}

	public void emptyAddress() {
		addresses = null;
	}

	public void updateAddress(int index, UserAddress newAddress) {
		addresses = toModifiableList(addresses);
		this.addresses.set(index, newAddress);
	}

	public void removeAddress(UserAddress address) {
		addresses = toModifiableList(addresses);
		this.addresses.remove(address);
	}

	public void softDelete(String deletedBy) {
		this.deletedAt = LocalDateTime.now();
		this.deletedBy = deletedBy;
	}

	public boolean isDeleted() {
		return this.deletedAt != null;
	}

	private <T> List<T> toModifiableList(List<T> items) {
		return new ArrayList<>(Objects.requireNonNullElseGet(items, ArrayList::new));
	}
}

